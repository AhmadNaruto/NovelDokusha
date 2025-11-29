package my.noveldokusha.network

import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

/* Default yang dipakai ulang agar tidak buat objek baru setiap kali */
private val DEFAULT_CACHE_CONTROL = CacheControl.Builder()
    .maxAge(10, TimeUnit.MINUTES)
    .build()
private val DEFAULT_HEADERS = Headers.Builder().build()

/**
 * Builder khusus FormUrlEncoded.
 * Contoh:
 *   postPayload { add("key","value") }
 */
fun Request.Builder.postPayload(
    scope: FormBody.Builder.() -> Unit
): Request.Builder {
    val form = FormBody.Builder().apply(scope).build()
    return post(form)
}

/**
 * GET request dengan parameter opsional.
 */
fun getRequest(
    url: String,
    headers: Headers = DEFAULT_HEADERS,
    cache: CacheControl = DEFAULT_CACHE_CONTROL
): Request = Request.Builder()
    .url(url)
    .headers(headers)
    .cacheControl(cache)
    .build()

/**
 * POST request.
 * [body] default null → akan diisi oleh caller (FormBody, Multipart, dsb).
 */
fun postRequest(
    url: String,
    headers: Headers = DEFAULT_HEADERS,
    body: RequestBody? = null,
    cache: CacheControl = DEFAULT_CACHE_CONTROL
): Request = Request.Builder()
    .url(url)
    .apply { body?.let { post(it) } } // hanya post bila body ada
    .headers(headers)
    .cacheControl(cache)
    .build()
