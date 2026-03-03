package my.noveldokusha.network

import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

/**
 * Default cache control policy: max-age 10 minutes.
 */
private val DEFAULT_CACHE_CONTROL = CacheControl.Builder()
    .maxAge(10, TimeUnit.MINUTES)
    .build()

/**
 * Empty headers instance.
 */
private val EMPTY_HEADERS = Headers.Builder().build()

/**
 * Empty request body instance.
 */
private val EMPTY_BODY: RequestBody = FormBody.Builder().build()

/**
 * Builds a GET request with optional headers and cache control.
 */
fun Request.Builder.get(
    url: String,
    headers: Headers = EMPTY_HEADERS,
    cacheControl: CacheControl = DEFAULT_CACHE_CONTROL
): Request.Builder = apply {
    url(url)
    headers(headers)
    cacheControl(cacheControl)
}

/**
 * Builds a POST request with optional headers, body, and cache control.
 */
fun Request.Builder.post(
    url: String,
    headers: Headers = EMPTY_HEADERS,
    body: RequestBody = EMPTY_BODY,
    cacheControl: CacheControl = DEFAULT_CACHE_CONTROL
): Request.Builder = apply {
    url(url)
    post(body)
    headers(headers)
    cacheControl(cacheControl)
}

/**
 * Builds a POST request with a form body constructed from the given scope.
 */
fun Request.Builder.post(
    url: String,
    headers: Headers = EMPTY_HEADERS,
    cacheControl: CacheControl = DEFAULT_CACHE_CONTROL,
    bodyBuilder: FormBody.Builder.() -> Unit
): Request.Builder {
    val formBody = FormBody.Builder().apply(bodyBuilder).build()
    return post(url, headers, formBody, cacheControl)
}

/**
 * Convenience function to build a GET request.
 */
fun getRequest(
    url: String,
    headers: Headers = EMPTY_HEADERS,
    cacheControl: CacheControl = DEFAULT_CACHE_CONTROL
): Request.Builder = Request.Builder().get(url, headers, cacheControl)

/**
 * Convenience function to build a POST request.
 */
fun postRequest(
    url: String,
    headers: Headers = EMPTY_HEADERS,
    body: RequestBody = EMPTY_BODY,
    cacheControl: CacheControl = DEFAULT_CACHE_CONTROL
): Request.Builder = Request.Builder().post(url, headers, body, cacheControl)

/**
 * Convenience function to build a POST request with form body.
 */
fun postRequest(
    url: String,
    headers: Headers = EMPTY_HEADERS,
    cacheControl: CacheControl = DEFAULT_CACHE_CONTROL,
    bodyBuilder: FormBody.Builder.() -> Unit
): Request.Builder {
    val formBody = FormBody.Builder().apply(bodyBuilder).build()
    return Request.Builder().post(url, headers, formBody, cacheControl)
}
