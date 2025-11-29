package my.noveldokusha.network

import android.net.Uri


/* ---------- Uri Extension ---------- */

/**
 * Parse String ke Uri; bila gagal return null (bukan crash).
 */
fun String.toUrl(): Uri? = runCatching { Uri.parse(this) }.getOrNull()

/**
 * Parse lalu langsung buildUpon(); bila gagal throw (dipakai bila yakin valid).
 */
fun String.toUrlBuilderSafe(): Uri.Builder =
    checkNotNull(toUrl()?.buildUpon()) { "Invalid URL: $this" }

fun String.toUrlBuilder(): Uri.Builder? = toUrl()?.buildUpon()

/**
 * Conditional builder.
 */
inline fun Uri.Builder.ifCase(
    case: Boolean,
    action: Uri.Builder.() -> Uri.Builder
): Uri.Builder = if (case) action(this) else this

/**
 * Tambah beberapa path sekaligus.
 */
fun Uri.Builder.addPath(vararg path: String): Uri.Builder =
    path.fold(this) { b, seg -> b.appendPath(seg) }

/**
 * Tambah query parameter berpasangan.
 */
fun Uri.Builder.add(vararg query: Pair<String, Any>): Uri.Builder =
    query.fold(this) { b, (k, v) -> b.appendQueryParameter(k, v.toString()) }

/**
 * Tambah single query parameter (versi singkat).
 */
fun Uri.Builder.add(key: String, value: Any): Uri.Builder =
    appendQueryParameter(key, value.toString())
