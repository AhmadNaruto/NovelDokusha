package my.noveldokusha.scraper

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

/**
 * Interface defining a novel source.
 */
sealed interface SourceInterface {
    /**
     * Unique identifier for this source.
     */
    val id: String

    /**
     * String resource ID for the source name.
     */
    @get:StringRes
    val nameStrId: Int

    /**
     * Base URL of the source.
     */
    val baseUrl: String

    /**
     * Whether this source requires authentication.
     */
    val requiresLogin: Boolean get() = false

    /**
     * Character set used for parsing responses.
     */
    val charset: String get() = "UTF-8"

    /**
     * Transforms a chapter URL to the preferred format.
     */
    fun transformChapterUrl(url: String): String = url

    /**
     * Extracts the chapter title from a document.
     */
    suspend fun getChapterTitle(doc: Document): String? = null

    /**
     * Extracts the chapter text content from a document.
     */
    suspend fun getChapterText(doc: Document): String? = null

    /**
     * A basic source without catalog functionality.
     */
    interface Base : SourceInterface

    /**
     * A source with catalog functionality (browsable library).
     */
    interface Catalog : SourceInterface {
        /**
         * URL to the main catalog page.
         */
        val catalogUrl: String

        /**
         * Language code of the source content.
         */
        val language: LanguageCode?

        /**
         * URL to the source icon.
         */
        val iconUrl: Any get() = "$baseUrl/favicon.ico"

        /**
         * Fetches the book cover image URL.
         */
        suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> =
            Response.Success(null)

        /**
         * Fetches the book description.
         */
        suspend fun getBookDescription(bookUrl: String): Response<String?> =
            Response.Success(null)

        /**
         * Fetches the list of chapters for a book.
         * Chapters are ordered from oldest to newest.
         */
        suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>>

        /**
         * Fetches a page from the catalog.
         * @param index Zero-based page index.
         */
        suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>>

        /**
         * Searches the catalog.
         * @param index Zero-based page index.
         * @param input Search query.
         */
        suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>>
    }

    /**
     * A source that can be configured via a settings screen.
     */
    interface Configurable {
        @Composable
        fun SettingsScreen()
    }
}
