package my.noveldokusha.network

import android.net.Uri
import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

// ============================================
// Backward compatibility aliases
// These redirect old API names to the new ones
// ============================================

/**
 * @deprecated Use [toUriBuilder] instead
 */
@Deprecated("Use toUriBuilder", ReplaceWith("toUriBuilder()"))
fun String.toUrlBuilder(): Uri.Builder? = toUriBuilder()

/**
 * @deprecated Use [toUriBuilderSafe] instead
 */
@Deprecated("Use toUriBuilderSafe", ReplaceWith("toUriBuilderSafe()"))
fun String.toUrlBuilderSafe(): Uri.Builder = toUriBuilderSafe()

/**
 * @deprecated Use [toUri] instead
 */
@Deprecated("Use toUri", ReplaceWith("toUri()"))
fun String.toUrl(): Uri? = toUri()

/**
 * @deprecated Use [appendQuery] instead
 */
@Deprecated("Use appendQuery", ReplaceWith("appendQuery(key, value)"))
fun Uri.Builder.add(key: String, value: Any): Uri.Builder =
    appendQuery(key, value)

/**
 * @deprecated Use [appendPaths] instead
 */
@Deprecated("Use appendPaths", ReplaceWith("appendPaths(*path)"))
fun Uri.Builder.addPath(vararg path: String) = appendPaths(*path)

/**
 * @deprecated Use [appendQueries] instead
 */
@Deprecated("Use appendQueries", ReplaceWith("appendQueries(*query)"))
fun Uri.Builder.add(vararg query: Pair<String, Any>) = appendQueries(*query)

/**
 * @deprecated Use [ifTrue] instead
 */
@Deprecated("Use ifTrue", ReplaceWith("ifTrue(case) { action(this) }"))
fun Uri.Builder.ifCase(case: Boolean, action: Uri.Builder.() -> Uri.Builder) =
    ifTrue(case) { action(this) }

/**
 * @deprecated Use [execute] instead
 */
@Deprecated("Use execute", ReplaceWith("execute(builder, followRedirects)"))
suspend fun NetworkClient.call(builder: Request.Builder, followRedirects: Boolean = false) =
    execute(builder, followRedirects)

/**
 * @deprecated Use [executeRequest] instead
 */
@Deprecated("Use executeRequest", ReplaceWith("executeRequest(builder)"))
suspend fun OkHttpClient.call(builder: Request.Builder) = executeRequest(builder)

/**
 * Builds a POST request with a form body constructed from the given scope.
 * @deprecated Use [postRequest] with scope instead
 */
@Deprecated("Use postRequest with scope", ReplaceWith("postRequest(url, headers, cacheControl, scope)"))
inline fun Request.Builder.postPayload(scope: FormBody.Builder.() -> Unit): Request.Builder {
    val builder = FormBody.Builder()
    scope(builder)
    return post(builder.build())
}
