package my.noveldokusha.network.interceptors

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import my.noveldokusha.core.domain.CloudfareVerificationBypassFailedException
import my.noveldokusha.core.domain.WebViewCookieManagerInitializationFailedException
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.seconds

private val CLOUDFLARE_ERROR_CODES = setOf(
    HttpsURLConnection.HTTP_FORBIDDEN,    // 403
    HttpsURLConnection.HTTP_UNAVAILABLE   // 503
)

private val CLOUDFLARE_SERVER_HEADERS = setOf("cloudflare-nginx", "cloudflare")

private const val TAG = "CloudflareInterceptor"
private const val CF_CLEARANCE_COOKIE = "cf_clearance"
private const val CHALLENGE_TIMEOUT_SECONDS = 120
private const val COOKIE_SYNC_DELAY_MILLIS = 500L
private const val EXTRA_FLUSH_DELAY_MILLIS = 2000L

/**
 * Interceptor that detects Cloudflare security challenges and resolves them
 * using a WebView to complete the verification.
 */
internal class CloudFareVerificationInterceptor(
    @ApplicationContext private val appContext: Context
) : Interceptor {

    private val lock = ReentrantLock()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!isCloudflareChallenge(response)) {
            return response
        }

        return lock.withLock {
            handleCloudflareChallenge(chain, request, response)
        }
    }

    private fun handleCloudflareChallenge(
        chain: Interceptor.Chain,
        request: Request,
        initialResponse: Response
    ): Response {
        val cookieManager = CookieManager.getInstance()
            ?: throw WebViewCookieManagerInitializationFailedException()

        initialResponse.close()
        clearCloudflareCookies(request.url, cookieManager)

        // First attempt: resolve with WebView
        runBlocking(Dispatchers.IO) {
            resolveWithWebView(request.url.toString(), cookieManager, forceChallenge = false)
        }

        val webViewUserAgent = WebSettings.getDefaultUserAgent(appContext)
        val cookies = cookieManager.getCookie(request.url.toString()).orEmpty()

        Timber.d("Cookies after first attempt: cf_clearance=${cookies.contains(CF_CLEARANCE_COOKIE)}")

        val retryRequest = request.newBuilder()
            .header("Cookie", cookies)
            .header("User-Agent", webViewUserAgent)
            .build()

        val retryResponse = chain.proceed(retryRequest)

        if (!isCloudflareChallenge(retryResponse)) {
            Timber.d("Cloudflare bypass successful on first retry")
            return retryResponse
        }

        // Second attempt: force fresh challenge
        Timber.w("First retry failed, forcing fresh challenge")
        retryResponse.close()

        return forceFreshChallenge(chain, request, cookieManager, webViewUserAgent)
    }

    private fun forceFreshChallenge(
        chain: Interceptor.Chain,
        request: Request,
        cookieManager: CookieManager,
        webViewUserAgent: String
    ): Response {
        val domain = request.url.host
        Timber.d("Clearing all cookies for domain: $domain")

        // Aggressive cookie clearing
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        cookieManager.removeSessionCookies(null)
        cookieManager.flush()

        // Wait for cookies to be fully cleared
        Thread.sleep(COOKIE_SYNC_DELAY_MILLIS)

        // Resolve with fresh challenge
        runBlocking(Dispatchers.IO) {
            resolveWithWebView(request.url.toString(), cookieManager, forceChallenge = true)
        }

        val freshCookies = cookieManager.getCookie(request.url.toString()).orEmpty()
        Timber.d("Fresh cookies: cf_clearance=${freshCookies.contains(CF_CLEARANCE_COOKIE)}")

        val finalRequest = request.newBuilder()
            .header("Cookie", freshCookies)
            .header("User-Agent", webViewUserAgent)
            .build()

        val finalResponse = chain.proceed(finalRequest)

        if (isCloudflareChallenge(finalResponse)) {
            Timber.e("Cloudflare bypass failed after fresh challenge")
            throw CloudfareVerificationBypassFailedException()
        }

        Timber.d("Cloudflare bypass successful after fresh challenge")
        return finalResponse
    }

    private fun isCloudflareChallenge(response: Response): Boolean =
        response.code in CLOUDFLARE_ERROR_CODES &&
                response.header("Server") in CLOUDFLARE_SERVER_HEADERS

    private fun clearCloudflareCookies(url: HttpUrl, cookieManager: CookieManager) {
        val urlString = url.toString()
        val existingCookies = cookieManager.getCookie(urlString).orEmpty()
        val filteredCookies = existingCookies
            .splitToSequence(";")
            .map { it.trim() }
            .filter { !it.startsWith("$CF_CLEARANCE_COOKIE=") }
            .joinToString("; ")

        cookieManager.setCookie(urlString, filteredCookies)
        cookieManager.flush()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun resolveWithWebView(
        url: String,
        cookieManager: CookieManager,
        forceChallenge: Boolean
    ) = withContext(Dispatchers.Default) {
        Timber.d("Starting Cloudflare challenge resolution for: $url (force=$forceChallenge)")

        // Check if valid cookie already exists
        if (!forceChallenge) {
            cookieManager.flush()
            val existingCookies = cookieManager.getCookie(url).orEmpty()
            if (existingCookies.contains(CF_CLEARANCE_COOKIE)) {
                Timber.d("Valid cf_clearance cookie already exists")
                return@withContext
            }
        }

        // Launch WebView for manual challenge
        withContext(Dispatchers.Main) {
            val intent = Intent().apply {
                setClassName(appContext, "my.noveldokusha.webview.WebViewActivity")
                putExtra("url", url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
        }

        // Wait for challenge completion
        val initialCookies = cookieManager.getCookie(url).orEmpty()
        val hadClearanceInitially = initialCookies.contains(CF_CLEARANCE_COOKIE)
        val minWaitSeconds = if (forceChallenge && hadClearanceInitially) 3 else 1

        var attempts = 0
        var challengeSolved = false

        while (!challengeSolved && attempts < CHALLENGE_TIMEOUT_SECONDS) {
            delay(1.seconds)
            attempts++

            cookieManager.flush()
            val allCookies = cookieManager.getCookie(url).orEmpty()

            if (attempts % 10 == 0) {
                Timber.d("Waiting for challenge: $attempts/$CHALLENGE_TIMEOUT_SECONDS seconds")
            }

            if (attempts >= minWaitSeconds && allCookies.contains(CF_CLEARANCE_COOKIE)) {
                if (forceChallenge && hadClearanceInitially) {
                    if (allCookies != initialCookies) {
                        Timber.d("Challenge solved: cookie changed after $attempts seconds")
                        challengeSolved = true
                    }
                } else {
                    Timber.d("Challenge solved: cf_clearance found after $attempts seconds")
                    challengeSolved = true
                }
            }
        }

        if (!challengeSolved) {
            Timber.w("Challenge timeout after $attempts attempts")
        }

        // Extra time for cookie sync
        if (challengeSolved) {
            cookieManager.flush()
            delay(EXTRA_FLUSH_DELAY_MILLIS)
        }
    }
}
