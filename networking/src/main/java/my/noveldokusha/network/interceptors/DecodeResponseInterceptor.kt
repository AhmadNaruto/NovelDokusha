package my.noveldokusha.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.internal.http.promisesBody
import okio.GzipSource
import okio.buffer
import okio.source
import org.brotli.dec.BrotliInputStream
import java.util.zip.InflaterInputStream

/**
 * Dekode response ber-encoding br, gzip, ataupun deflate.
 * Content-Length dihapus karena ukuran setelah dekompresi tidak diketahui.
 */
internal class DecodeResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.promisesBody()) return response

        val body = response.body
        val encoding = response.header("Content-Encoding")?.lowercase() ?: return response

        val decompressed = when (encoding) {
            "br"    -> BrotliInputStream(body.source().inputStream()).source().buffer()
            "gzip"  -> GzipSource(body.source()).buffer()
            "deflate" -> InflaterInputStream(body.source().inputStream()).source().buffer()
            else    -> return response
        }

        return response.newBuilder()
            .removeHeader("Content-Encoding") // hilangkan agar client tidak proses ulang
            .removeHeader("Content-Length")   // ukuran sudah berubah
            .body(decompressed.asResponseBody(body.contentType(), -1))
            .build()
    }
}
