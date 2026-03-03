package my.noveldokusha.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.internal.http.promisesBody
import okio.GzipSource
import okio.buffer
import okio.source
import org.brotli.dec.BrotliInputStream

/**
 * Interceptor that decompresses response bodies encoded with:
 * - brotli (br)
 * - gzip
 */
internal class DecodeResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (!response.promisesBody()) {
            return response
        }

        val body = response.body ?: return response
        val contentEncoding = response.header("Content-Encoding")?.lowercase() ?: return response

        val decompressedSource = when (contentEncoding) {
            "br" -> BrotliInputStream(body.source().inputStream()).source().buffer()
            "gzip" -> GzipSource(body.source()).buffer()
            else -> return response
        }

        // Content length is unknown after decompression
        return response.newBuilder()
            .removeHeader("Content-Encoding")
            .removeHeader("Content-Length")
            .body(decompressedSource.asResponseBody(body.contentType(), -1))
            .build()
    }
}
