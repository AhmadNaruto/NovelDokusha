package com.supermarkdown

/**
 * Heading style for markdown output.
 */
enum class HeadingStyle {
    /** ATX style: `# Heading` */
    Atx,

    /** Setext style: `Heading\n======` */
    Setext,
}

/**
 * Link style for markdown output.
 */
enum class LinkStyle {
    /** Inline style: `[text](url)` */
    Inline,

    /** Referenced style: `[text][1]` with references at bottom */
    Referenced,
}

/**
 * Options for HTML to Markdown conversion.
 *
 * @param headingStyle Heading style (ATX or Setext). Default: Atx.
 * @param linkStyle Link style (Inline or Referenced). Default: Inline.
 * @param codeFence Character for code fences. Default: '`'.
 * @param bulletMarker Character for unordered list markers. Default: '-'.
 * @param baseUrl Base URL for resolving relative links. Default: null.
 * @param excludeSelectors CSS selectors for elements to exclude. Default: empty.
 * @param includeSelectors CSS selectors for elements to force include (overrides excludes). Default: empty.
 */
data class SupermarkdownOptions(
    val headingStyle: HeadingStyle = HeadingStyle.Atx,
    val linkStyle: LinkStyle = LinkStyle.Inline,
    val codeFence: Char = '`',
    val bulletMarker: Char = '-',
    val baseUrl: String? = null,
    val excludeSelectors: List<String> = emptyList(),
    val includeSelectors: List<String> = emptyList(),
) {
    internal fun headingStyleCode(): Int = when (headingStyle) {
        HeadingStyle.Atx -> 0
        HeadingStyle.Setext -> 1
    }

    internal fun linkStyleCode(): Int = when (linkStyle) {
        LinkStyle.Inline -> 0
        LinkStyle.Referenced -> 1
    }

    internal fun excludeString(): String = excludeSelectors.joinToString(",")
    internal fun includeString(): String = includeSelectors.joinToString(",")
}

/**
 * High-level HTML to Markdown converter.
 *
 * This is the main entry point for converting HTML to Markdown in Android apps.
 * It wraps the native Rust library via JNI.
 *
 * Example usage:
 * ```kotlin
 * val converter = SupermarkdownConverter()
 *
 * // Simple conversion
 * val markdown = converter.convert("<h1>Hello</h1><p>World</p>")
 *
 * // With custom options
 * val options = SupermarkdownOptions(
 *     headingStyle = HeadingStyle.Setext,
 *     excludeSelectors = listOf("nav", ".sidebar", "footer")
 * )
 * val markdown = converter.convert(html, options)
 * ```
 *
 * @throws UnsatisfiedLinkError if the native library fails to load.
 */
class SupermarkdownConverter {

    /**
     * Get the library version.
     */
    fun version(): String = SupermarkdownNative.version()

    /**
     * Convert HTML to Markdown with default options.
     *
     * @param html The HTML string to convert.
     * @return The converted Markdown string.
     */
    fun convert(html: String): String {
        return SupermarkdownNative.convert(html)
    }

    /**
     * Convert HTML to Markdown with custom options.
     *
     * @param html The HTML string to convert.
     * @param options Conversion options.
     * @return The converted Markdown string.
     */
    fun convert(html: String, options: SupermarkdownOptions): String {
        return SupermarkdownNative.convertWithOptions(
            html = html,
            headingStyle = options.headingStyleCode(),
            linkStyle = options.linkStyleCode(),
            codeFence = options.codeFence.toString(),
            bulletMarker = options.bulletMarker.toString(),
            baseUrl = options.baseUrl ?: "",
            excludeSelectors = options.excludeString(),
            includeSelectors = options.includeString(),
        )
    }
}
