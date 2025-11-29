package my.noveldokusha.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.random.Random

/**
 * Menambahkan jeda acak antar-request agar tidak membentuk pola yang mudah dideteksi.
 * Thread.sleep di sini aman karena OkHttp mengeksekusi interceptor di background thread.
 */
class DelayInterceptor(
    private val min: Long,  // millis
    private val max: Long   // millis
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val sleep = Random.nextLong(min, max)
        Thread.sleep(sleep) // blocking, tapi di IO thread
        return chain.proceed(chain.request())
    }
}
