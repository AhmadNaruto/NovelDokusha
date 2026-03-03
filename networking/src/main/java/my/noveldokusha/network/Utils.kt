package my.noveldokusha.network

import android.net.Uri

/**
 * Safely converts a string to a [Uri.Builder], returning null if the string is not a valid URI.
 */
fun String.toUriBuilder(): Uri.Builder? = toUri()?.buildUpon()

/**
 * Converts a string to a [Uri], returning null if parsing fails.
 */
fun String.toUri(): Uri? = runCatching { Uri.parse(this) }.getOrNull()

/**
 * Converts a string to a [Uri.Builder], throwing an exception if the string is not a valid URI.
 * Use this when you're certain the string is a valid URI.
 */
fun String.toUriBuilderSafe(): Uri.Builder =
    toUri()?.buildUpon() ?: throw IllegalArgumentException("Invalid URI: $this")

/**
 * Conditionally applies a transformation to the [Uri.Builder] based on the given condition.
 */
inline fun Uri.Builder.ifTrue(condition: Boolean, action: Uri.Builder.() -> Unit): Uri.Builder =
    apply { if (condition) action() }

/**
 * Appends multiple path segments to the URI.
 */
fun Uri.Builder.appendPaths(vararg paths: String): Uri.Builder =
    paths.fold(this) { builder, path -> builder.appendPath(path) }

/**
 * Appends multiple query parameters to the URI.
 */
fun Uri.Builder.appendQueries(vararg queries: Pair<String, Any>): Uri.Builder =
    queries.fold(this) { builder, (key, value) ->
        builder.appendQueryParameter(key, value.toString())
    }

/**
 * Appends a single query parameter to the URI.
 */
fun Uri.Builder.appendQuery(key: String, value: Any): Uri.Builder =
    appendQueryParameter(key, value.toString())
