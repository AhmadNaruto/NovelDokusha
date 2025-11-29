package my.noveldokusha.network.interceptors

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

/* -------------------- Konstanta -------------------- */
private val ERROR_CODES = listOf(
    HttpsURLConnection.HTTP_FORBIDDEN,  // 403
    HttpsURLConnection.HTTP_UNAVAILABLE // 503
)
private val SERVER_CHECK = arrayOf("cloudflare-nginx", "cloudflare")
private const val TAG = "CloudflareInterceptor"

/**
 * Interceptor yang menangani tantangan keamanan Cloudflare.
 * Jika mendeteksi halaman tantanan, buat [WebViewActivity] untuk
 * penyelesaian manual oleh pengguna.
 */
internal class CloudflareVerificationInterceptor(
    @ApplicationContext private val appContext: Context
) : Interceptor {

    private val lock = ReentrantLock()

    /* ------------- Entry-point Interceptor ------------- */
    @Throws(IOException::class, CancellationException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        /* Bukan Cloudflare → langsung return */
        if (isNotCloudflare(response)) return response

        /* Hindari race condition saat multiple thread menemui tantangan bersamaan */
        return lock.withLock {
            try {
                val cookieManager = CookieManager.getInstance()
                    ?: throw WebViewCookieManagerInitializationFailedException()

                response.close() // tutup response sebelumnya supaya tidak leak

                /* Hapus cf_clearance lama agar tidak tertukar */
                val oldCookie = cookieManager.getCookie(request.url.toString()).orEmpty()
                val cleanedCookie = oldCookie
                    .splitToSequence(";")
                    .map { it.split("=").map(String::trim) }
                    .filter { it.firstOrNull() != "cf_clearance" }
                    .joinToString(";") { "${it[0]}=${it.getOrElse(1) { "" }}" }

                cookieManager.setCookie(request.url.toString(), cleanedCookie)

                /* Serangan pertama: coba pakai cookie yang sudah ada (tanpa force) */
                runBlocking(Dispatchers.IO) {
                    resolveWithWebView(request, cookieManager, forceChallenge = false)
                }

                val cookies = cookieManager.getCookie(request.url.toString()).orEmpty()
                val webViewUa = WebSettings.getDefaultUserAgent(appContext)

                val newRequest = request.newBuilder()
                    .header("Cookie", cookies)
                    .header("User-Agent", webViewUa)
                    .build()

                val retryResp = chain.proceed(newRequest)

                /* Masih diblok? Bersihkan cookie & paksa tantangan baru */
                if (!isNotCloudflare(retryResp)) {
                    retryResp.close()
                    val domain = request.url.host
                    cookieManager.removeAllCookies(null)
                    cookieManager.removeSessionCookies(null)
                    cookieManager.flush()
                    Thread.sleep(500) // tunggu penghapusan selesai

                    runBlocking(Dispatchers.IO) {
                        resolveWithWebView(request, cookieManager, forceChallenge = true)
                    }

                    val freshCookies = cookieManager.getCookie(request.url.toString()).orEmpty()
                    val finalRequest = request.newBuilder()
                        .header("Cookie", freshCookies)
                        .header("User-Agent", webViewUa)
                        .build()

                    val finalResp = chain.proceed(finalRequest)
                    if (!isNotCloudflare(finalResp)) {
                        throw CloudfareVerificationBypassFailedException()
                    }
                    return@withLock finalResp
                }

                retryResp // berhasil bypass
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                throw e
            } catch (e: Exception) {
                throw IOException(e.message, e.cause)
            }
        }
    }

    /* ----------- Helper ----------- */
    private fun isNotCloudflare(response: Response): Boolean =
        response.code !in ERROR_CODES ||
        response.header("Server") !in SERVER_CHECK

    /* ----------- WebView launcher ----------- */
    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun resolveWithWebView(
        request: Request,
        cookieManager: CookieManager,
        forceChallenge: Boolean
    ) = withContext(Dispatchers.Default) { /* sama seperti Anda, tidak diubah */ }
}
