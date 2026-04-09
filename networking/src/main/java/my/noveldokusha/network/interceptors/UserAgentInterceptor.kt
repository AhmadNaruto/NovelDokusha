package my.noveldokusha.network.interceptors

import my.noveldokusha.core.appPreferences.AppPreferences
import okhttp3.Interceptor
import okhttp3.Response


internal class UserAgentInterceptor(
    private val appPreferences: AppPreferences
) : Interceptor {

    companion object {
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64)"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val hasNoUserAgent = originalRequest.header("User-Agent").isNullOrBlank()
        val customUserAgent = appPreferences.NETWORK_USER_AGENT.value.takeIf { it.isNotBlank() }
            ?: DEFAULT_USER_AGENT

        val modifiedRequest = if (hasNoUserAgent) {
            originalRequest
                .newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", customUserAgent)
                .build()
        } else originalRequest
        return chain.proceed(modifiedRequest)
    }
}