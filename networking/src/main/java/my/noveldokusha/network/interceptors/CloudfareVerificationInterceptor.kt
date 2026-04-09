package my.noveldokusha.network.interceptors

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.core.domain.CloudfareVerificationBypassFailedException
import my.noveldokusha.core.domain.WebViewCookieManagerInitializationFailedException
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.seconds

private val ERROR_CODES = listOf(
    HttpsURLConnection.HTTP_FORBIDDEN /*403*/,
    HttpsURLConnection.HTTP_UNAVAILABLE /*503*/
)
private val SERVER_CHECK = arrayOf("cloudflare-nginx", "cloudflare")

private const val TAG = "CloudflareInterceptor"
private const val PREFS_NAME = "cloudflare_cookies"
private const val COOKIE_EXPIRY_MS = 24L * 60 * 60 * 1000 // 24 hours

/**
 * If a CloudFlare security verification redirection is detected, execute a
 * WebView and retrieve the necessary headers.
 *
 * Optimizations:
 * - Cookie persistence: saves cf_clearance to SharedPreferences so it survives app restarts
 * - Faster detection: polls every 500ms instead of 1s
 * - Configurable timeout via AppPreferences
 * - Proper cookie parsing (fixes bug where '=' in cookie values breaks splitting)
 */
internal class CloudFareVerificationInterceptor(
    @ApplicationContext private val appContext: Context,
    private val appPreferences: AppPreferences
) : Interceptor {

    private val lock = ReentrantLock()
    private val cookiePrefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (isNotCloudFare(response)) {
            return response
        }

        return lock.withLock {
            try {
                val cookieManager = CookieManager.getInstance()
                    ?: throw WebViewCookieManagerInitializationFailedException()

                response.close()

                // Restore persisted cf_clearance cookie if available
                restorePersistedCookie(cookieManager, request)

                // Remove old cf_clearance from the cookie to force fresh challenge
                val cookie = cookieManager
                    .getCookie(request.url.toString())
                    ?.splitCookiePreservingValues()
                    ?.filter { it.first != "cf_clearance" }
                    ?.joinToString(";") { "${it.first}=${it.second}" }

                if (!cookie.isNullOrBlank()) {
                    cookieManager.setCookie(request.url.toString(), cookie)
                }

                runBlocking(Dispatchers.IO) {
                    resolveWithWebView(request, cookieManager, forceChallenge = false)
                }

                // Get the cookies and add them to the request
                val cookies = cookieManager.getCookie(request.url.toString()) ?: ""
                Log.d(TAG, "Full cookies for retry: $cookies")
                Log.d(TAG, "cf_clearance present: ${cookies.contains("cf_clearance")}")

                // Persist cf_clearance for future sessions
                persistCfClearance(cookies, request.url.toString())

                // Get the User-Agent that WebView used (must match for Cloudflare)
                val webViewUserAgent = WebSettings.getDefaultUserAgent(appContext)
                Log.d(TAG, "Using WebView User-Agent: $webViewUserAgent")

                val newRequest = request.newBuilder()
                    .header("Cookie", cookies)
                    .header("User-Agent", webViewUserAgent)
                    .build()

                val responseCloudfare = chain.proceed(newRequest)
                Log.d(TAG, "Retry response: code=${responseCloudfare.code}, server=${responseCloudfare.header("Server")}")

                if (!isNotCloudFare(responseCloudfare)) {
                    Log.w(TAG, "Retry still blocked by Cloudflare after cookie set - forcing fresh challenge")
                    responseCloudfare.close()

                    // Clear ALL cookies for this domain to ensure fresh start
                    val domain = request.url.host
                    Log.d(TAG, "Clearing ALL cookies for domain: $domain")

                    cookieManager.removeAllCookies(null)
                    cookieManager.flush()
                    cookieManager.removeSessionCookies(null)
                    cookieManager.flush()

                    // Clear persisted cookie too
                    cookiePrefs.edit()
                        .remove(domain)
                        .remove("${domain}_timestamp")
                        .apply()

                    // Wait a bit for cookies to be fully cleared
                    Thread.sleep(500)

                    Log.d(TAG, "Cookies cleared, launching WebView for fresh challenge")
                    runBlocking(Dispatchers.IO) {
                        resolveWithWebView(request, cookieManager, forceChallenge = true)
                    }

                    // Retry one more time with fresh cookie
                    val freshCookies = cookieManager.getCookie(request.url.toString()) ?: ""
                    Log.d(TAG, "Fresh cookies after challenge: cf_clearance present=${freshCookies.contains("cf_clearance")}")

                    // Persist fresh cf_clearance
                    persistCfClearance(freshCookies, request.url.toString())

                    val finalRequest = request.newBuilder()
                        .header("Cookie", freshCookies)
                        .header("User-Agent", webViewUserAgent)
                        .build()

                    val finalResponse = chain.proceed(finalRequest)
                    Log.d(TAG, "Final retry response: code=${finalResponse.code}")

                    if (!isNotCloudFare(finalResponse)) {
                        Log.e(TAG, "Still blocked after fresh challenge - giving up")
                        throw CloudfareVerificationBypassFailedException()
                    }

                    return@withLock finalResponse
                } else {
                    Log.d(TAG, "Successfully bypassed Cloudflare!")
                }

                responseCloudfare
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                throw e
            } catch (e: Exception) {
                throw IOException(e.message, e.cause)
            }
        }
    }

    private fun isNotCloudFare(response: Response): Boolean {
        return response.code !in ERROR_CODES ||
                response.header("Server") !in SERVER_CHECK
    }

    /**
     * Parse cookies correctly handling '=' characters in values.
     * Original code used .split("=") which breaks when cookie values contain '='.
     */
    private fun String.splitCookiePreservingValues(): List<Pair<String, String>> {
        return splitToSequence(";")
            .mapNotNull { cookieStr ->
                val trimmed = cookieStr.trim()
                val eqIndex = trimmed.indexOf('=')
                if (eqIndex > 0) {
                    trimmed.substring(0, eqIndex) to trimmed.substring(eqIndex + 1)
                } else null
            }
            .toList()
    }

    /**
     * Restore persisted cf_clearance cookie from SharedPreferences.
     */
    private fun restorePersistedCookie(cookieManager: CookieManager, request: Request) {
        val domain = request.url.host
        val savedCookie = cookiePrefs.getString(domain, null)
        val savedTimestamp = cookiePrefs.getLong("${domain}_timestamp", 0L)
        val now = System.currentTimeMillis()

        if (savedCookie != null && (now - savedTimestamp) < COOKIE_EXPIRY_MS) {
            Log.d(TAG, "Restoring persisted cf_clearance for $domain (age: ${(now - savedTimestamp) / 60000}min)")
            cookieManager.setCookie(request.url.toString(), savedCookie)
        } else if (savedCookie != null) {
            Log.d(TAG, "Persisted cf_clearance expired for $domain, removing")
            cookiePrefs.edit()
                .remove(domain)
                .remove("${domain}_timestamp")
                .apply()
        }
    }

    /**
     * Persist cf_clearance cookie to SharedPreferences for future sessions.
     */
    private fun persistCfClearance(cookies: String, url: String) {
        if (!appPreferences.CLOUDFLARE_COOKIE_PERSISTENCE_ENABLED.value) return

        val cfClearance = cookies.splitCookiePreservingValues()
            .find { it.first == "cf_clearance" }
            ?.second

        if (cfClearance != null) {
            val domain = android.net.Uri.parse(url)?.host ?: return
            cookiePrefs.edit()
                .putString(domain, "cf_clearance=$cfClearance")
                .putLong("${domain}_timestamp", System.currentTimeMillis())
                .apply()
            Log.d(TAG, "Persisted cf_clearance for $domain")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun resolveWithWebView(
        request: Request,
        cookieManager: CookieManager,
        forceChallenge: Boolean = false
    ): Unit = withContext(Dispatchers.Default) {
        val url = request.url.toString()
        val domain = request.url.host
        val timeoutSeconds = appPreferences.CLOUDFLARE_CHALLENGE_TIMEOUT_SECONDS.value

        Log.d(TAG, "Starting Cloudflare challenge resolution for: $url")
        Log.d(TAG, "Domain: $domain, timeout: ${timeoutSeconds}s")

        // Check if we already have a valid cf_clearance cookie (unless forced)
        if (!forceChallenge) {
            cookieManager.flush()
            val existingCookies = cookieManager.getCookie(url) ?: ""

            if (existingCookies.contains("cf_clearance")) {
                Log.d(TAG, "cf_clearance cookie already exists, no WebView needed")
                return@withContext
            }
        } else {
            Log.d(TAG, "Forcing fresh challenge (existing cookie was invalid)")
        }

        // Launch WebView for manual challenge
        Log.d(TAG, "Launching WebView for manual challenge")
        withContext(Dispatchers.Main) {
            val intent = Intent().apply {
                setClassName(appContext, "my.noveldokusha.webview.WebViewActivity")
                putExtra("url", url)
                putExtra("challenge_mode", "cloudflare")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
        }

        // Wait for the user to solve the challenge
        val pollIntervalMs = 500L // Poll every 500ms for faster detection
        val maxAttempts = (timeoutSeconds * 1000L / pollIntervalMs).toInt()
        var attempts = 0
        var challengeSolved = false

        // Store the initial cookie state to detect NEW cookies
        cookieManager.flush()
        val initialCookies = cookieManager.getCookie(url) ?: ""
        val hadClearanceInitially = initialCookies.contains("cf_clearance")

        // If forcing challenge, we expect the cookie to change
        val minWaitSeconds = if (forceChallenge && hadClearanceInitially) 3 else 1
        val minWaitAttempts = (minWaitSeconds * 1000L / pollIntervalMs).toInt()

        Log.d(TAG, "Waiting for challenge resolution (pollInterval=${pollIntervalMs}ms, minWait=${minWaitAttempts}attempts, hadClearance=$hadClearanceInitially)")

        while (!challengeSolved && attempts < maxAttempts) {
            delay(pollIntervalMs)
            attempts++

            // Flush cookies to ensure they're written
            cookieManager.flush()

            // Get ALL cookies from the page
            val allCookies = cookieManager.getCookie(url) ?: ""

            // Log cookies every 10 seconds for debugging
            if (attempts % 20 == 0) {
                Log.d(TAG, "Attempt $attempts/$maxAttempts - Waiting for cf_clearance...")
            }

            // Only accept cookie after minimum wait time
            if (attempts >= minWaitAttempts && allCookies.contains("cf_clearance")) {
                // If we're forcing a challenge and had a cookie initially,
                // verify the cookie value changed
                if (forceChallenge && hadClearanceInitially) {
                    if (allCookies != initialCookies) {
                        Log.d(TAG, "cf_clearance cookie changed! Challenge solved after ${attempts * pollIntervalMs / 1000}s.")
                        challengeSolved = true
                    } else {
                        Log.d(TAG, "cf_clearance found but unchanged, continuing to wait...")
                    }
                } else {
                    Log.d(TAG, "cf_clearance cookie found! Challenge solved after ${attempts * pollIntervalMs / 1000}s.")
                    challengeSolved = true
                }
            }
        }

        if (!challengeSolved) {
            Log.w(TAG, "Challenge NOT solved after ${attempts * pollIntervalMs / 1000}s")
        }

        // Give extra time for cookies to fully sync
        if (challengeSolved) {
            cookieManager.flush()
            delay(2.seconds)
        }
    }
}