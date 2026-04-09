package my.noveldokusha.network

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import my.noveldokusha.core.AppInternalState
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.network.interceptors.CloudFareVerificationInterceptor
import my.noveldokusha.network.interceptors.DecodeResponseInterceptor
import my.noveldokusha.network.interceptors.UserAgentInterceptor
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface NetworkClient {
    suspend fun call(request: Request.Builder, followRedirects: Boolean = false): Response
    suspend fun get(url: String): Response
    suspend fun get(url: Uri.Builder): Response
}

@Singleton
class ScraperNetworkClient @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appInternalState: AppInternalState,
    private val appPreferences: AppPreferences,
) : NetworkClient {

    private val cookieJar = ScraperCookieJar()

    private val okhttpLoggingInterceptor = HttpLoggingInterceptor {
        Timber.v(it)
    }.apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    // No HTTP cache — OkHttp always fetches fresh data.
    // Image caching is handled solely by Coil's disk/memory cache.
    val client = OkHttpClient.Builder()
        .cache(null) // Disable OkHttp cache entirely
        .let {
            if (appInternalState.isDebugMode) {
                it.addInterceptor(okhttpLoggingInterceptor)
            } else it
        }
        .addInterceptor(UserAgentInterceptor(appPreferences))
        .addInterceptor(DecodeResponseInterceptor())
        .addInterceptor(CloudFareVerificationInterceptor(appContext, appPreferences))
        .cookieJar(cookieJar)
        .connectionPool(
            ConnectionPool(
                maxIdleConnections = 10,
                keepAliveDuration = 5,
                timeUnit = TimeUnit.MINUTES,
            )
        )
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val clientWithRedirects = client
        .newBuilder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override suspend fun call(request: Request.Builder, followRedirects: Boolean): Response {
        return if (followRedirects) clientWithRedirects.call(request) else client.call(request)
    }

    override suspend fun get(url: String) = call(getRequest(url))
    override suspend fun get(url: Uri.Builder) = call(getRequest(url.toString()))
}
