package my.noveldokusha.scraper.templates

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.appendQuery
import my.noveldokusha.network.appendPaths
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUriBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Element

/**
 * Template for ReadNovelFull-style sites.
 *
 * Common pattern:
 * - Book page: /novel-name.html
 * - Chapters loaded via AJAX from separate endpoint
 * - Search: /novel-list/search?keyword=query
 * - Catalog: /novel-list/most-popular-novel
 *
 * Used by: ReadNovelFull, BestLightNovel, NovelHall, etc.
 */
abstract class BaseNovelFullScraper(
    networkClient: NetworkClient
) : BaseScraperTemplate(networkClient) {

    // Default selectors for NovelFull-style sites
    override val selectBookCover: String = ".book img[src]"
    override val selectBookDescription: String = "#tab-description"
    override val selectChapterList: String = "a[href]"
    override val selectChapterContent: String = "#chr-content"
    override val selectCatalogItems: String = ".row"
    override val selectCatalogItemTitle: String = "a[href]"
    override val selectCatalogItemUrl: String = "a[href]"
    override val selectCatalogItemCover: String = "img[src]"
    override val selectPaginationLastPage: String = "ul.pagination li:last-child.disabled"

    // Search selectors
    override val selectSearchItems: String = ".row"
    override val selectSearchItemTitle: String = "a[href]"
    override val selectSearchItemUrl: String = "a[href]"
    override val selectSearchItemCover: String = "img[src]"

    // Configuration
    protected open val novelListPath: String = "novel-list"
    protected open val catalogOrderBy: String = "most-popular-novel"

    // AJAX chapter loading
    protected open val useAjaxChapterLoading: Boolean = true
    protected open val ajaxChapterPath: String = "ajax/chapter-archive"
    protected open val novelIdAttribute: String = "data-novel-id"
    protected open val novelIdSelector: String = "#rating[$novelIdAttribute]"

    override fun buildCatalogUrl(index: Int): String {
        val page = index + 1
        return catalogUrl.toUriBuilderSafe()
            .appendQuery("page", page)
            .toString()
    }

    override fun buildSearchUrl(index: Int, query: String): String {
        val page = index + 1
        return baseUrl.toUriBuilderSafe()
            .appendPaths(novelListPath, "search")
            .appendQuery("keyword", query)
            .appendQuery("page", page)
            .toString()
    }

    override suspend fun fetchChapterList(bookUrl: String): List<ChapterResult> =
        if (useAjaxChapterLoading) {
            fetchChapterListViaAjax(bookUrl)
        } else {
            super.fetchChapterList(bookUrl)
        }

    protected open suspend fun fetchChapterListViaAjax(bookUrl: String): List<ChapterResult> =
        withContext(Dispatchers.Default) {
            try {
                val doc = networkClient.get(bookUrl).toDocument()
                val novelId = doc.selectFirst(novelIdSelector)
                    ?.attr(novelIdAttribute)
                    ?: throw Exception("Novel ID not found")

                val ajaxUrl = baseUrl + ajaxChapterPath + "?novelId=" + novelId

                networkClient.get(ajaxUrl).toDocument()
                    .select(selectChapterList)
                    .mapNotNull { element ->
                        element.toChapterResult()
                    }
            } catch (_: Exception) {
                // Fallback to regular chapter list
                super.fetchChapterList(bookUrl)
            }
        }

    private fun Element.toChapterResult(): ChapterResult? {
        val title = text().takeIf { it.isNotEmpty() } ?: return null
        val url = attr("href").takeIf { it.isNotEmpty() } ?: return null
        return ChapterResult(title, transformChapterUrl(url))
    }
}
