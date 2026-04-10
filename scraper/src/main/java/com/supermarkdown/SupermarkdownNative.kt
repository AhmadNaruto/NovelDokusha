package com.supermarkdown

/**
 * Native JNI interface to the supermarkdown Rust library.
 *
 * This class handles loading the native library and provides low-level JNI bindings.
 * For a higher-level API, use [SupermarkdownConverter] instead.
 */
internal object SupermarkdownNative {

    init {
        System.loadLibrary("supermarkdown")
    }

    /**
     * Convert HTML to Markdown with default options.
     *
     * @param html The HTML string to convert.
     * @return The converted Markdown string.
     */
    external fun convert(html: String): String

    /**
     * Convert HTML to Markdown with custom options.
     *
     * @param html The HTML string to convert.
     * @param headingStyle 0 = ATX, 1 = Setext
     * @param linkStyle 0 = Inline, 1 = Referenced
     * @param codeFence Single character for code fences (default: '`')
     * @param bulletMarker Single character for list markers (default: '-')
     * @param baseUrl Base URL for resolving relative links (nullable)
     * @param excludeSelectors Comma-separated CSS selectors to exclude
     * @param includeSelectors Comma-separated CSS selectors to force include
     * @return The converted Markdown string.
     */
    external fun convertWithOptions(
        html: String,
        headingStyle: Int,
        linkStyle: Int,
        codeFence: String,
        bulletMarker: String,
        baseUrl: String,
        excludeSelectors: String,
        includeSelectors: String,
    ): String

    /**
     * Get the library version.
     *
     * @return The version string from Cargo.toml.
     */
    external fun version(): String
}
