package my.noveldokusha.network

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

internal class ScraperCookieJar : CookieJar {
    private val manager = CookieManager.getInstance().also {
        it.setAcceptCookie(true)
        // Note: setAcceptThirdPartyCookies should be called from app initialization
    }

    private fun getCookieList(url: String?): List<String> {
        url ?: return emptyList()
        manager.flush() // Ensure we get latest cookies from disk
        return manager.getCookie(url)?.split(";")?.filter { it.isNotBlank() } ?: emptyList()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return getCookieList(url.toString()).mapNotNull { Cookie.parse(url, it) }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        for (cookieEntry in cookies) {
            val cookieString = buildCookieString(cookieEntry)
            manager.setCookie(urlString, cookieString)
        }
        manager.flush() // Persist to disk immediately
    }

    private fun buildCookieString(cookie: Cookie): String {
        val sb = StringBuilder()
        sb.append("${cookie.name}=${cookie.value}")
        if (cookie.domain != null) {
            sb.append("; domain=${cookie.domain}")
        }
        if (cookie.path != null) {
            sb.append("; path=${cookie.path}")
        }
        if (cookie.expiresAt != Long.MIN_VALUE) {
            val maxAge = (cookie.expiresAt - System.currentTimeMillis()) / 1000
            if (maxAge > 0) {
                sb.append("; Max-Age=$maxAge")
            }
        }
        if (cookie.secure) {
            sb.append("; secure")
        }
        if (cookie.httpOnly) {
            sb.append("; httponly")
        }
        return sb.toString()
    }
}
