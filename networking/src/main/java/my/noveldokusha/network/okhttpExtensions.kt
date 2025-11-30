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

suspend fun OkHttpClient.call(builder: Request.Builder) = newCall(builder.build()).await()

// Perbaikan: Optimalkan parsing dokumen dengan menghindari pembuatan string yang tidak perlu
fun Response.toDocument(): Document {
    // Perbaikan: Gunakan body.byteStream() untuk mengurangi overhead konversi string
    return body.byteStream().use { inputStream ->
        Jsoup.parse(inputStream, null, request.url.toString())
    }
}

// Perbaikan: Optimalkan parsing dokumen dengan charset spesifik
fun Response.toDocument(charset: String): Document {
    val bytes = body.bytes()
    val html = String(bytes, Charset.forName(charset))
    val baseUrl = request.url.toString()
    return Jsoup.parse(html, baseUrl)
}

// Perbaikan: Optimalkan parsing JSON untuk mengurangi konsumsi memori
fun Response.toJson(): JsonElement {
    // Perbaikan: Gunakan body.byteStream() dan parsing langsung dari stream
    return body.byteStream().use { inputStream ->
        JsonParser.parseReader(inputStream.reader())
    }
}