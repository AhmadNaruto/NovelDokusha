
// getting no Chapter.
//
package my.noveldokusha.scraper.sources

import com.chimbori.crux.articles.extractImages
import my.noveldokusha.data.BookMetadata
import my.noveldokusha.data.ChapterMetadata
import my.noveldokusha.scraper.*
import org.jsoup.nodes.Document

class WebNovelLight : SourceInterface.catalog
{
    override val name = "Web Novel Light"
    override val baseUrl = "https://webnovellight.com/"
    override val catalogUrl = "https://webnovellight.com/series/?m_orderby=alphabet"
    override val language = "English"


    override suspend fun getBookCoverImageUrl(doc: Document): String?
    {
        return doc.selectFirst("div.summary_image")
            ?.selectFirst("img[src]")
            ?.attr("src")
    }

    override suspend fun getBookDescripton(doc: Document): String?
    {
        return doc.selectFirst(".summary__content.show-more")
            ?.let { textExtractor.get(it) }
    }

    override suspend fun getChapterList(doc: Document): List<ChapterMetadata>
    {
        val url = doc
            .location()
            .toUrlBuilder()
            ?.addPath("ajax")
            ?.addPath("chapters")

        return connect(url.toString())
            .addHeaderRequest()
            .postIO()
            .select(".wp-manga-chapter > a[href]")
            .map { ChapterMetadata(title = it.text(), url = it.attr("href")) }
            .reversed()
    }

    override suspend fun getCatalogList(index: Int): Response<List<BookMetadata>>
    {
        val url = "https://webnovellight.com/wp-admin/admin-ajax.php"
        return tryConnect {
            connect(url)
                .addHeaderRequest()
                .data("action", "madara_load_more")
                .data("page", index.toString())
                .data("template", "madara-core/content/content-archive")
                .data("vars[paged]", "1")
                .data("vars[orderby]", "post_title")
                .data("vars[template]", "archive")
                .data("vars[sidebar]", "right")
                .data("vars[post_type]", "wp-manga")
                .data("vars[post_status]", "publish")
                .data("vars[order]", "ASC")
                .data("vars[meta_query][relation]", "OR")
                .data("vars[manga_archives_item_layout]", "default")
                .postIO()
                .select(".post-title.font-title")
                .mapNotNull { it.selectFirst("a[href]") }
                .map { BookMetadata(title = it.text(), url = it.attr("href")) }
                .let { Response.Success(it) }
        }
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<List<BookMetadata>>
    {
        val url = "https://webnovellight.com/wp-admin/admin-ajax.php"
        return tryConnect {
            connect(url)
                .addHeaderRequest()
                .data("action", "madara_load_more")
                .data("page", index.toString())
                .data("template", "madara-core/content/content-search")
                .data("vars[s]", input)
                .data("vars[orderby]", "")
                .data("vars[paged]", "1")
                .data("vars[template]", "search")
                .data("vars[meta_query][0][relation]", "AND")
                .data("vars[meta_query][relation]", "OR")
                .data("vars[post_type]", "wp-manga")
                .data("vars[post_status]", "publish")
                .data("vars[manga_archives_item_layout]", "default")
                .postIO()
                .select("div.post-title")
                .mapNotNull { it.selectFirst("a[href]") }
                .map { BookMetadata(title = it.text(), url = it.attr("href")) }
                .let { Response.Success(it) }
        }
    }
}