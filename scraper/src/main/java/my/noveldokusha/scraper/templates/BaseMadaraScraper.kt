package my.noveldokusha.scraper.templates

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.appendQuery
import my.noveldokusha.network.appendPaths
import my.noveldokusha.network.postRequest
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUriBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.domain.ChapterResult
import okhttp3.FormBody
import org.jsoup.nodes.Element

/**
 * Template for WordPress Madara theme-based sites.
 *
 * Common pattern:
 * - WordPress with Madara manga/novel theme
 * - Book page: /novel-name/
 * - Chapters loaded via AJAX POST to /ajax/chapters/
 * - Search: /?s=query&post_type=wp-manga
 * - Uses data-src for lazy-loading images
 *
 * Used by: BoxNovel, NovelMultiverse, WuxiaWorld.site, etc.
 */
abstract class BaseMadaraScraper(
    networkClient: NetworkClient
) : BaseScraperTemplate(networkClient) {

    // Default selectors for Madara-style sites
    override val selectBookCover: String = ".summary_image img[data-src]"
    override val selectBookDescription: String = ".summary__content.show-more"
    override val selectChapterList: String = ".wp-manga-chapter a[href]"
    override val selectChapterContent: String = ".reading-content"
    override val selectCatalogItems: String = ".page-item-detail"
    override val selectCatalogItemTitle: String = "a[href]"
    override val selectCatalogItemUrl: String = "a[href]"
    override val selectCatalogItemCover: String = "img[data-src]"
    override val selectPaginationLastPage: String = "div.nav-previous.float-left"

    // Search selectors
    override val selectSearchItems: String = ".c-tabs-item__content"
    override val selectSearchItemTitle: String = ".post-title h3 a"
    override val selectSearchItemUrl: String = ".post-title h3 a"
    override val selectSearchItemCover: String = "img[data-src]"

    // Configuration
    protected open val catalogPath: String = "novel"
    protected open val catalogOrderBy: String = "alphabet"
    protected open val useAjaxChapterLoading: Boolean = true
    protected open val reverseChapterOrder: Boolean = true

    override fun buildCatalogUrl(index: Int): String {
        val page = index + 1
        return baseUrl.toUriBuilderSafe()
            .appendPaths(catalogPath)
            .apply {
                if (page > 1) appendPaths("page", page.toString())
                appendQuery("m_orderby", catalogOrderBy)
            }
            .toString()
    }

    override fun buildSearchUrl(index: Int, query: String): String {
        val page = index + 1
        return baseUrl.toUriBuilderSafe()
            .apply {
                if (page > 1) appendPaths("page", page.toString())
                appendQuery("s", query)
                appendQuery("post_type", "wp-manga")
                appendQuery("op", "")
                appendQuery("author", "")
                appendQuery("artist", "")
                appendQuery("release", "")
                appendQuery("adult", "")
            }
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
                val cleanUrl = bookUrl.substringBefore("?").trimEnd('/')
                val ajaxUrl = "$cleanUrl/ajax/chapters/"

                val chapters = networkClient.get(ajaxUrl).toDocument()
                    .select(selectChapterList)
                    .mapNotNull { element ->
                        element.toChapterResult()
                    }

                if (reverseChapterOrder) chapters.reversed() else chapters
            } catch (_: Exception) {
                try {
                    fetchChapterListAlternate(bookUrl)
                } catch (_: Exception) {
                    super.fetchChapterList(bookUrl)
                }
            }
        }

    protected open suspend fun fetchChapterListAlternate(bookUrl: String): List<ChapterResult> =
        withContext(Dispatchers.Default) {
            val doc = networkClient.get(bookUrl).toDocument()
            val mangaId = doc.selectFirst("#manga-chapters-holder[data-id]")
                ?.attr("data-id")
                ?: throw Exception("Manga ID not found")

            val ajaxUrl = baseUrl.toUriBuilderSafe()
                .appendPaths("wp-admin", "admin-ajax.php")
                .toString()

            val formBody = FormBody.Builder()
                .add("action", "manga_get_chapters")
                .add("manga", mangaId)
                .build()

            val chapters = networkClient.post(ajaxUrl, formBody).toDocument()
                .select(selectChapterList)
                .mapNotNull { element ->
                    element.toChapterResult()
                }

            if (reverseChapterOrder) chapters.reversed() else chapters
        }

    override fun isLastPage(doc: org.jsoup.nodes.Document): Boolean =
        doc.selectFirst(selectPaginationLastPage) == null

    private fun Element.toChapterResult(): ChapterResult? {
        val title = text().takeIf { it.isNotEmpty() } ?: return null
        val url = attr("href").takeIf { it.isNotEmpty() } ?: return null
        return ChapterResult(title, transformChapterUrl(url))
    }
}
