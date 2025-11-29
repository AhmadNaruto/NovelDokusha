package my.noveldokusha.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Menambah header umum agar setiap request tampak datang dari browser yang berbeda.
 */
class RandomHeadersInterceptor : Interceptor {

    private val langs = listOf(
        "en-US,en;q=0.9",
        "id-ID,id;q=0.9,en;q=0.8"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val newReq = chain.request().newBuilder()
            .header("Accept-Language", langs.random())
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "none")
            .build()
        return chain.proceed(newReq)
    }
}
