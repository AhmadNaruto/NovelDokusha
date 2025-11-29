package my.noveldokusha.network

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import my.noveldokusha.core.AppInternalState
import my.noveldokusha.network.interceptors.UserAgentInterceptor
import my.noveldokusha.network.interceptors.RandomHeadersInterceptor
import my.noveldokusha.network.interceptors.DelayInterceptor
import my.noveldokusha.network.interceptors.DecodeResponseInterceptor
import my.noveldokusha.network.interceptors.CloudflareVerificationInterceptor // FIXED: Ubah dari CloudFareVerificationInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ConnectionSpec
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File
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
    private val appInternalState: AppInternalState
) : NetworkClient {

    private val cacheDir = File(appContext.cacheDir, "network_cache")
    private val cacheSize = 50L * 1024 * 1024 // 50 MB

    private val cookieJar = ScraperCookieJar()

    private val loggingInterceptor = HttpLoggingInterceptor { Timber.v(it) }.apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val baseClient = OkHttpClient.Builder().apply {
        if (appInternalState.isDebugMode) addInterceptor(loggingInterceptor)
        addInterceptor(UserAgentInterceptor())
        addInterceptor(RandomHeadersInterceptor())
        addInterceptor(DelayInterceptor(min = 800, max = 2_000))
        addInterceptor(DecodeResponseInterceptor())
        addInterceptor(CloudflareVerificationInterceptor(appContext)) // FIXED: Ubah dari CloudFareVerificationInterceptor
        cookieJar(cookieJar)
        cache(Cache(cacheDir, cacheSize))
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
    }.build()
    
    internal val client: OkHttpClient get() = baseClient

    private val clientWithRedirects = baseClient.newBuilder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override suspend fun call(
        request: Request.Builder,
        followRedirects: Boolean
    ): Response = withContext(Dispatchers.IO) {
        val client = if (followRedirects) clientWithRedirects else baseClient
        kotlinx.coroutines.delay(kotlin.random.Random.nextLong(50, 300))
        client.newCall(request.build()).execute()
    }

    override suspend fun get(url: String): Response = call(Request.Builder().url(url))
    override suspend fun get(url: Uri.Builder): Response = get(url.build().toString())
}
