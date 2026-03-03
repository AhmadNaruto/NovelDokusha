package my.noveldokusha.network

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import my.noveldokusha.core.AppInternalState
import my.noveldokusha.network.interceptors.CloudFareVerificationInterceptor
import my.noveldokusha.network.interceptors.DecodeResponseInterceptor
import my.noveldokusha.network.interceptors.UserAgentInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for making HTTP requests.
 */
interface NetworkClient {
    /**
     * Executes an HTTP request.
     * @param request The request builder.
     * @param followRedirects Whether to follow redirects. Defaults to false.
     */
    suspend fun execute(request: Request.Builder, followRedirects: Boolean = false): Response

    /**
     * Executes a GET request to the specified URL.
     */
    suspend fun get(url: String): Response

    /**
     * Executes a GET request to the specified URL builder.
     */
    suspend fun get(url: Uri.Builder): Response

    /**
     * Executes a POST request to the specified URL with the given body.
     */
    suspend fun get(url: String, body: RequestBody): Response
}

/**
 * Default implementation of [NetworkClient] using OkHttp.
 */
@Singleton
class ScraperNetworkClient @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appInternalState: AppInternalState,
) : NetworkClient {

    private val cacheDir = File(appContext.cacheDir, "network_cache")
    private val cacheSize = 5L * 1024 * 1024 // 5 MB

    private val cookieJar = ScraperCookieJar()

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.v(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val baseClient = OkHttpClient.Builder()
        .apply {
            if (appInternalState.isDebugMode) {
                addInterceptor(loggingInterceptor)
            }
        }
        .addInterceptor(UserAgentInterceptor())
        .addInterceptor(DecodeResponseInterceptor())
        .addInterceptor(CloudFareVerificationInterceptor(appContext))
        .cookieJar(cookieJar)
        .cache(Cache(cacheDir, cacheSize))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val clientWithRedirects = baseClient.newBuilder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override suspend fun execute(request: Request.Builder, followRedirects: Boolean): Response {
        val client = if (followRedirects) clientWithRedirects else baseClient
        return client.executeRequest(request)
    }

    override suspend fun get(url: String): Response = execute(getRequest(url))

    override suspend fun get(url: Uri.Builder): Response = execute(getRequest(url.toString()))

    override suspend fun get(url: String, body: RequestBody): Response =
        execute(postRequest(url, body = body))
}
