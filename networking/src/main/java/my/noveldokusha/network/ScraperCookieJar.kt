package my.noveldokusha.network

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

internal class ScraperCookieJar : CookieJar {

    private val manager = CookieManager.getInstance().also {
        it.setAcceptCookie(true)
    }

    /**
     * Membaca seluruh cookie untuk *url* lalu parse menjadi List<Cookie>.
     * Flush sekali agar hasil paling fresh.
     */
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        manager.flush()                       // <-- tambahan: pastitas cookie terbaru
        val raw = manager.getCookie(url.toString()).orEmpty()
        return raw.split(';')
            .map { it.trim() }                // <-- hilangkan spasi
            .filter { it.isNotEmpty() }
            .mapNotNull { Cookie.parse(url, it) }
    }

    /**
     * Menyimpan cookie hasil response ke CookieManager.
     * Flush sekali di akhir sudah cukup karena CM thread-safe.
     */
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlStr = url.toString()
        for (c in cookies) {
            manager.setCookie(urlStr, "${c.name}=${c.value}")
        }
        manager.flush()
    }
}
