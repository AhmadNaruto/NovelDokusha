package my.noveldokusha.network

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import kotlin.coroutines.resumeWithException

/**
 * Ekstensi suspend untuk menunggu Response tanpa okhttp-coroutine.
 * FIXED: Gunakan suspendCancellableCoroutine dengan proper cancellation handling.
 */
suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            cont.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            // FIXED: Resume hanya dengan response, tidak ada lambda onCancellation
            cont.resume(response)
        }
    })
    
    // FIXED: Handle cancellation properly
    cont.invokeOnCancellation {
        cancel() // Batalkan OkHttp call jika coroutine di-cancel
    }
}

fun Response.toDocument(): Document = body.use { body ->
    Jsoup.parse(body.string(), request.url.toString())
}

fun Response.toDocument(charset: String): Document = body.use { body ->
    val html = String(body.bytes(), java.nio.charset.Charset.forName(charset))
    Jsoup.parse(html, request.url.toString())
}

fun Response.toJson(): JsonElement = body.use { body ->
    JsonParser.parseString(body.string())
}
