package my.noveldokusha.network

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

internal class ScraperCookieJar : CookieJar {
    // Perbaikan: Gunakan singleton untuk CookieManager agar tidak membuat instance berulang
    private val manager = CookieManager.getInstance().also {
        it.setAcceptCookie(true)
    }

    // Tambahkan cache lokal untuk mengurangi permintaan ke CookieManager
    private val cookieCache = ConcurrentHashMap<String, List<Cookie>>()

    private fun getCookieList(url: String?): List<String> {
        url ?: return emptyList()
        return manager.getCookie(url)?.split(";") ?: emptyList()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val urlString = url.toString()

        // Perbaikan: Gunakan cache lokal untuk mengurangi permintaan ke CookieManager
        return cookieCache[urlString] ?: run {
            val cookies = getCookieList(urlString).mapNotNull {
                Cookie.parse(url, it.trim())
            }
            // Simpan cookie ke cache untuk URL ini
            cookieCache[urlString] = cookies
            cookies
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        // Perbaikan: Proses penyimpanan cookie lebih efisien
        val urlString = url.toString()
        for (cookie in cookies) {
            // Perbaikan: Gunakan string builder untuk menggabungkan nama dan nilai cookie
            val cookieString = StringBuilder()
                .append(cookie.name)
                .append("=")
                .append(cookie.value)
                .toString()

            manager.setCookie(urlString, cookieString)
        }
        manager.flush()

        // Hapus cache untuk URL ini agar tidak usang
        cookieCache.remove(urlString)
    }
}
