package my.noveldokusha.network

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import timber.log.Timber

/**
 * CookieJar implementation that uses Android's WebView CookieManager.
 * This allows sharing cookies between OkHttp and WebView (useful for Cloudflare challenges).
 */
internal class ScraperCookieJar : CookieJar {

    private val cookieManager = CookieManager.getInstance().apply {
        setAcceptCookie(true)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val urlString = url.toString()
        val cookies = cookieManager.getCookie(urlString)
            ?.split(";")
            ?.mapNotNull { Cookie.parse(url, it.trim()) }
            .orEmpty()

        Timber.v("Loading ${cookies.size} cookies for $urlString")
        return cookies
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        cookies.forEach { cookie ->
            val cookieString = "${cookie.name}=${cookie.value}"
            cookieManager.setCookie(urlString, cookieString)
        }
        cookieManager.flush()
        Timber.v("Saved ${cookies.size} cookies from $urlString")
    }
}
