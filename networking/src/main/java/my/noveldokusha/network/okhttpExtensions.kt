package my.noveldokusha.network

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Suspends and awaits the completion of an OkHttp [Call], returning the [Response].
 * Executes on [Dispatchers.IO] by default.
 */
private suspend fun Call.await(): Response = withContext(Dispatchers.IO) {
    suspendCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }
        })
    }
}

/**
 * Executes an HTTP request using the [OkHttpClient] and returns the [Response].
 */
suspend fun OkHttpClient.executeRequest(builder: Request.Builder): Response =
    newCall(builder.build()).await()

/**
 * Parses the response body as an HTML [Document].
 */
fun Response.toDocument(): Document {
    val html = body.string()
    val baseUrl = request.url.toString()
    return Jsoup.parse(html, baseUrl)
}

/**
 * Parses the response body as an HTML [Document] with the specified charset.
 */
fun Response.toDocument(charset: String): Document {
    val bytes = body.bytes()
    val html = String(bytes, Charset.forName(charset))
    val baseUrl = request.url.toString()
    return Jsoup.parse(html, baseUrl)
}

/**
 * Parses the response body as a JSON [JsonElement].
 */
fun Response.toJson(): JsonElement {
    return JsonParser.parseString(body.string())
}
