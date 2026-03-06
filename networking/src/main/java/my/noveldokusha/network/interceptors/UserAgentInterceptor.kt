package my.noveldokusha.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds a default User-Agent header to requests that don't have one.
 * Default User-Agent mimics a mobile Android browser (UC Browser).
 */
internal class UserAgentInterceptor : Interceptor {

    companion object {
        private const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; U; Android 13; en-US; PFDM00 Build/TP1A.220905.001) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
                "Chrome/123.0.6312.80 UCBrowser/18.2.6.1452 Mobile Safari/537.36"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.header("User-Agent").isNullOrBlank()) {
            val modifiedRequest = originalRequest.newBuilder()
                .header("User-Agent", DEFAULT_USER_AGENT)
                .build()
            return chain.proceed(modifiedRequest)
        }

        return chain.proceed(originalRequest)
    }
}
