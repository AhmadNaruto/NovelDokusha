package my.noveldokusha.scraper.templates

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUriBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.network.tryFlatConnect
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Base template for web novel scrapers using the template method pattern.
 *
 * Concrete scrapers extend this class and provide CSS selectors specific to their site.
 * This reduces code duplication and provides consistent error handling.
 */
abstract class BaseScraperTemplate(
    protected val networkClient: NetworkClient
) : SourceInterface.Catalog {

    // ========================================
    // Required CSS Selectors
    // ========================================

    /** Selector for book cover image element */
    protected abstract val selectBookCover: String

    /** Selector for book description element */
    protected abstract val selectBookDescription: String

    /** Selector for chapter list container */
    protected abstract val selectChapterList: String

    /** Selector for chapter content element */
    protected abstract val selectChapterContent: String

    /** Selector for catalog items container */
    protected abstract val selectCatalogItems: String

    /** Selector for catalog item title */
    protected abstract val selectCatalogItemTitle: String

    /** Selector for catalog item URL */
    protected abstract val selectCatalogItemUrl: String

    /** Selector for catalog item cover image */
    protected abstract val selectCatalogItemCover: String

    // ========================================
    // Optional CSS Selectors
    // ========================================

    /** Selector for pagination last page indicator */
    protected open val selectPaginationLastPage: String? = null

    /** Selector for search results (defaults to catalog selectors) */
    protected open val selectSearchItems: String? = null
    protected open val selectSearchItemTitle: String? = null
    protected open val selectSearchItemUrl: String? = null
    protected open val selectSearchItemCover: String? = null

    // ========================================
    // URL Transformation Hooks
    // ========================================

    protected open fun transformBookUrl(url: String): String =
        url.ensureAbsolute(baseUrl)

    override fun transformChapterUrl(url: String): String =
        url.ensureAbsolute(baseUrl)

    protected open fun transformCoverUrl(url: String): String =
        url.ensureAbsolute(baseUrl)

    // ========================================
    // SourceInterface.Catalog Implementation
    // ========================================

    override suspend fun getChapterTitle(doc: Document): String? = null

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(selectChapterContent)?.let { TextExtractor.extract(it) }.orEmpty()
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> =
        withContext(Dispatchers.Default) {
            tryConnect {
                networkClient.get(bookUrl).toDocument()
                    .selectFirst(selectBookCover)
                    ?.extractImageUrl()
                    ?.let { transformCoverUrl(it) }
            }
        }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> =
        withContext(Dispatchers.Default) {
            tryConnect {
                networkClient.get(bookUrl).toDocument()
                    .selectFirst(selectBookDescription)
                    ?.let { TextExtractor.extract(it) }
            }
        }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> =
        withContext(Dispatchers.Default) {
            tryConnect { fetchChapterList(bookUrl) }
        }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> =
        withContext(Dispatchers.Default) {
            val url = buildCatalogUrl(index)
            tryFlatConnect("index=$index, url=$url") {
                val doc = networkClient.get(url).toDocument()
                parseCatalogPage(doc, index)
            }
        }

    override suspend fun getCatalogSearch(
        index: Int,
        query: String
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        if (query.isBlank()) {
            return@withContext Response.Success(PagedList.createEmpty(index))
        }

        val url = buildSearchUrl(index, query)
        tryFlatConnect("index=$index, query=$query, url=$url") {
            val doc = networkClient.get(url).toDocument()
            parseSearchPage(doc, index, query)
        }
    }

    // ========================================
    // Abstract Methods for Subclasses
    // ========================================

    /** Builds the catalog URL for the given page index. */
    protected abstract fun buildCatalogUrl(index: Int): String

    /** Builds the search URL for the given page index and query. */
    protected abstract fun buildSearchUrl(index: Int, query: String): String

    // ========================================
    // Protected Helper Methods
    // ========================================

    protected open suspend fun fetchChapterList(bookUrl: String): List<ChapterResult> =
        networkClient.get(bookUrl).toDocument()
            .select(selectChapterList)
            .mapNotNull { element ->
                val title = element.text().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                val url = element.attr("href").takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                ChapterResult(title, transformChapterUrl(url))
            }

    protected open suspend fun parseCatalogPage(
        doc: Document,
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val books = doc.select(selectCatalogItems)
                .mapNotNull { element -> parseCatalogItem(element) }

            PagedList(
                list = books,
                index = index,
                isLastPage = isLastPage(doc)
            )
        }
    }

    protected open suspend fun parseSearchPage(
        doc: Document,
        index: Int,
        query: String
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        val itemsSelector = selectSearchItems ?: selectCatalogItems
        val titleSelector = selectSearchItemTitle ?: selectCatalogItemTitle
        val urlSelector = selectSearchItemUrl ?: selectCatalogItemUrl
        val coverSelector = selectSearchItemCover ?: selectCatalogItemCover

        tryConnect {
            val books = doc.select(itemsSelector).mapNotNull { element ->
                val title = element.selectFirst(titleSelector)?.text() ?: return@mapNotNull null
                val url = element.selectFirst(urlSelector)?.attr("href") ?: return@mapNotNull null
                val cover = element.selectFirst(coverSelector)?.extractImageUrl().orEmpty()

                BookResult(
                    title = title,
                    url = transformBookUrl(url),
                    coverImageUrl = transformCoverUrl(cover)
                )
            }

            PagedList(
                list = books,
                index = index,
                isLastPage = isLastPage(doc)
            )
        }
    }

    protected open fun parseCatalogItem(element: Element): BookResult? {
        val title = element.selectFirst(selectCatalogItemTitle)?.text() ?: return null
        val url = element.selectFirst(selectCatalogItemUrl)?.attr("href") ?: return null
        val cover = element.selectFirst(selectCatalogItemCover)?.extractImageUrl().orEmpty()

        return BookResult(
            title = title,
            url = transformBookUrl(url),
            coverImageUrl = transformCoverUrl(cover)
        )
    }

    protected open fun isLastPage(doc: Document): Boolean {
        return selectPaginationLastPage?.let { selector ->
            doc.selectFirst(selector)?.let { element ->
                element.hasClass("disabled") || element.hasClass("active")
            } ?: true
        } ?: true
    }

    /**
     * Extracts image URL from an element, checking common lazy-loading attributes.
     */
    @Suppress("NOTHING_TO_INLINE")
    protected inline fun Element.extractImageUrl(): String =
        when {
            hasAttr("data-src") -> attr("data-src")
            hasAttr("src") -> attr("src")
            hasAttr("data-lazy-src") -> attr("data-lazy-src")
            else -> ""
        }

    /**
     * Normalizes image URLs by removing size constraints.
     */
    @Suppress("NOTHING_TO_INLINE")
    protected inline fun normalizeImageUrl(url: String): String = when {
        url.isEmpty() -> ""
        url.contains("novel_200_89") -> url.replace("novel_200_89", "novel")
        url.contains("t-200x89") -> url.replace("t-200x89", "t-300x439")
        else -> url
    }

    companion object {
        /**
         * Ensures a URL is absolute by prepending the base URL if necessary.
         */
        private fun String.ensureAbsolute(baseUrl: String): String =
            when {
                startsWith("http", ignoreCase = true) -> this
                startsWith("/") -> baseUrl.trimEnd('/') + this
                else -> baseUrl.trimEnd('/') + "/" + this
            }
    }
}
