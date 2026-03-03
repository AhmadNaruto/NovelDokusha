package my.noveldokusha.scraper.templates

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.appendPaths
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUriBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Element

/**
 * Template for NovelPub/LightNovelWorld-style sites.
 *
 * Common pattern:
 * - Modern novel reader with paginated chapter lists
 * - Book page: /novel/novel-name
 * - Chapters: /novel/novel-name/chapters/page-1, page-2, etc.
 * - Catalog: /genre/all/popular/all/
 *
 * Used by: LightNovelWorld, NovelPub, FreeWebNovel, etc.
 */
abstract class BaseNovelPubScraper(
    networkClient: NetworkClient
) : BaseScraperTemplate(networkClient) {

    // Default selectors for NovelPub-style sites
    override val selectBookCover: String = ".cover img[data-src]"
    override val selectBookDescription: String = ".summary .content"
    override val selectChapterList: String = ".chapter-list li a"
    override val selectChapterContent: String = "#chapter-container"
    override val selectCatalogItems: String = ".novel-item"
    override val selectCatalogItemTitle: String = "a[title]"
    override val selectCatalogItemUrl: String = "a[title]"
    override val selectCatalogItemCover: String = ".novel-cover img[data-src]"
    override val selectPaginationLastPage: String = "ul.pagination li:last-child.active"

    // Configuration
    protected open val genrePath: String = "genre"
    protected open val catalogGenre: String = "all"
    protected open val catalogSort: String = "popular"
    protected open val catalogStatus: String = "all"
    protected open val usesPaginatedChapterList: Boolean = true
    protected open val chaptersPerPage: Int = 100
    protected open val maxChapterPages: Int = 1000

    override fun buildCatalogUrl(index: Int): String {
        val page = index + 1
        return baseUrl.toUriBuilderSafe()
            .appendPaths(genrePath, catalogGenre, catalogSort, catalogStatus)
            .apply {
                if (page > 1) appendPath(page.toString())
            }
            .toString()
    }

    override fun buildSearchUrl(index: Int, query: String): String =
        catalogUrl // Search not supported by default

    override suspend fun getCatalogSearch(
        index: Int,
        query: String
    ): Response<PagedList<my.noveldokusha.scraper.domain.BookResult>> =
        Response.Success(PagedList.createEmpty(index))

    override suspend fun fetchChapterList(bookUrl: String): List<ChapterResult> =
        if (usesPaginatedChapterList) {
            fetchPaginatedChapterList(bookUrl)
        } else {
            super.fetchChapterList(bookUrl)
        }

    protected open suspend fun fetchPaginatedChapterList(bookUrl: String): List<ChapterResult> =
        withContext(Dispatchers.Default) {
            try {
                val chapters = mutableListOf<ChapterResult>()
                val baseChaptersUrl = bookUrl.toUriBuilderSafe().appendPaths("chapters")

                for (page in 1..maxChapterPages) {
                    val pageUrl = baseChaptersUrl.appendPaths("page-$page").toString()
                    val doc = networkClient.get(pageUrl).toDocument()
                    val pageChapters = doc.select(selectChapterList).mapNotNull { element ->
                        element.toChapterResult()
                    }

                    if (pageChapters.isEmpty()) break
                    chapters.addAll(pageChapters)
                }

                chapters
            } catch (_: Exception) {
                super.fetchChapterList(bookUrl)
            }
        }

    override fun isLastPage(doc: org.jsoup.nodes.Document): Boolean =
        doc.selectFirst("ul.pagination")?.let { pagination ->
            pagination.children().lastOrNull()?.hasClass("active") ?: true
        } ?: true

    private fun Element.toChapterResult(): ChapterResult? {
        val title = attr("title").ifBlank { text() }.takeIf { it.isNotEmpty() } ?: return null
        val url = attr("href").takeIf { it.isNotEmpty() } ?: return null
        return ChapterResult(title, transformChapterUrl(url))
    }
}
