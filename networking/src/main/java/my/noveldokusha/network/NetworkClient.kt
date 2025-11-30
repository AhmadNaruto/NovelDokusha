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
import okhttp3.Response
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
    private val appInternalState: AppInternalState,
) : NetworkClient {

    // Perbaikan: Meningkatkan ukuran cache untuk performa lebih baik dan mengurangi I/O
    // Menggunakan 50MB (sebelumnya 5MB) untuk cache jaringan
    private val cacheDir = File(appContext.cacheDir, "network_cache")
    private val cacheSize = 50L * 1024 * 1024 // 50MB cache size

    private val cookieJar = ScraperCookieJar()

    private val okhttpLoggingInterceptor = HttpLoggingInterceptor {
        Timber.v(it)
    }.apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    // Membuat instance OkHttpClient dengan konfigurasi optimal
    val client = OkHttpClient.Builder()
        .let {
            if (appInternalState.isDebugMode) {
                it.addInterceptor(okhttpLoggingInterceptor)
            } else it
        }
        .addInterceptor(UserAgentInterceptor())
        .addInterceptor(DecodeResponseInterceptor())
        .addInterceptor(CloudFareVerificationInterceptor(appContext))
        .cookieJar(cookieJar)
        .cache(Cache(cacheDir, cacheSize))
        .connectTimeout(15, TimeUnit.SECONDS)  // Perbaikan: Mengurangi timeout untuk respons lebih cepat
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)    // Menambahkan write timeout
        .retryOnConnectionFailure(true)        // Menambahkan retry untuk koneksi yang gagal
        .build()

    // Membuat instance terpisah dengan redirect aktif untuk menghindari pembuatan runtime
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
