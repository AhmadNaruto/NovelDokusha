package my.noveldokusha.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import my.noveldokusha.network.userAgentPool

/**
 * Menyisipkan User-Agent acak ke setiap request.
 */
internal class UserAgentInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val withUa = original.newBuilder()
            .header("User-Agent", userAgentPool.random())
            .build()
        return chain.proceed(withUa)
    }
}
