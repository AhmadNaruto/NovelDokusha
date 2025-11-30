package my.noveldokusha.network

import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

// Perbaikan: Gunakan lazy initialization untuk menghemat memori jika tidak digunakan
private val DEFAULT_CACHE_CONTROL by lazy { CacheControl.Builder().maxAge(10, TimeUnit.MINUTES).build() }
private val DEFAULT_HEADERS by lazy { Headers.Builder().build() }
private val DEFAULT_BODY by lazy { FormBody.Builder().build() }

// Perbaikan: Tambahkan fungsi untuk menghindari pembuatan FormBody kosong
fun Request.Builder.postPayload(scope: FormBody.Builder.() -> Unit = {}): Request.Builder {
    val builder = FormBody.Builder()
    scope(builder)
    val body = builder.build()
    // Perbaikan: Jika body kosong, gunakan DEFAULT_BODY alih-alih membuat objek baru
    return if (body.contentLength() == 0L) post(DEFAULT_BODY) else post(body)
}

fun getRequest(
    url: String,
    headers: Headers = DEFAULT_HEADERS,
    cache: CacheControl = DEFAULT_CACHE_CONTROL
) = Request.Builder()
    .url(url)
    .headers(headers)
    .cacheControl(cache)

// Perbaikan: Gunakan parameter default yang efisien
fun postRequest(
    url: String,
    headers: Headers = DEFAULT_HEADERS,
    body: RequestBody = DEFAULT_BODY,
    cache: CacheControl = DEFAULT_CACHE_CONTROL
) = Request.Builder()
    .url(url)
    .post(body)
    .headers(headers)
    .cacheControl(cache)
