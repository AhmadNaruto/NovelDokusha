package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.add
import my.noveldokusha.network.addPath
import my.noveldokusha.network.getRequest
import my.noveldokusha.network.ifCase
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUrlBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import okhttp3.Headers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * NovelBin scraper implementation untuk mengambil data novel dari novelbin.me
 * 
 * Source ini menyediakan:
 * - Katalog novel dengan pagination
 * - Pencarian novel berdasarkan keyword
 * - Detail buku (cover, deskripsi)
 * - Daftar chapter
 * - Konten chapter
 * 
 * @property networkClient Client untuk melakukan network requests
 */
class NovelBin(private val networkClient: NetworkClient) : SourceInterface.Catalog {
    
    override val id = "Novelbin"
    override val nameStrId = R.string.source_name_novelbin
    override val baseUrl = "https://novelbin.me/"
    override val catalogUrl = "https://novelbin.me/sort/novelbin-daily-update"
    override val iconUrl = "https://novelbin.me/img/logo.png"
    override val language = LanguageCode.ENGLISH

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Android 13; Mobile; rv:125.0) Gecko/125.0 Firefox/125.0"
        private const val SELECTOR_PAGINATION_DISABLED = "ul.pagination li.next.disabled"
        private const val SELECTOR_BOOK_LIST = "#list-page div.list-novel .row"
        private const val SELECTOR_BOOK_LINK = "div.col-xs-7 a"
        private const val SELECTOR_BOOK_COVER = "div.col-xs-3 > div > img"
        private const val SELECTOR_CHAPTER_TITLE = "h2 > .title-chapter"
        private const val SELECTOR_CHAPTER_TEXT = ".container .adsads"
        private const val SELECTOR_BOOK_COVER_META = "meta[itemprop=image]"
        private const val SELECTOR_BOOK_DESCRIPTION = "div.desc-text"
        private const val SELECTOR_OG_URL = "meta[property=og:url]"
        private const val SELECTOR_CHAPTER_LIST = "ul.list-chapter li a"
    }

    /**
     * Mengambil daftar buku dari halaman tertentu
     * 
     * @param index Index halaman (0-based)
     * @param url URL lengkap untuk mengambil data
     * @return Response berisi PagedList dari BookResult
     */
    private suspend fun getPagesList(index: Int, url: String): Response<PagedList<BookResult>> =
        withContext(Dispatchers.Default) {
            tryConnect {
                val doc = networkClient.get(url).toDocument()
                val isLastPage = doc.select(SELECTOR_PAGINATION_DISABLED).isNotEmpty()
                val bookResults = doc.select(SELECTOR_BOOK_LIST).mapNotNull { parseBookElement(it) }
                
                PagedList(
                    list = bookResults,
                    index = index,
                    isLastPage = isLastPage
                )
            }
        }

    /**
     * Parsing element HTML menjadi BookResult
     * 
     * @param element Element HTML yang berisi informasi buku
     * @return BookResult atau null jika parsing gagal
     */
    private fun parseBookElement(element: Element): BookResult? {
        val link = element.selectFirst(SELECTOR_BOOK_LINK) ?: return null
        val bookCover = element.selectFirst(SELECTOR_BOOK_COVER)?.attr("data-src").orEmpty()
        
        return BookResult(
            title = link.attr("title"),
            url = link.attr("href"),
            coverImageUrl = bookCover
        )
    }

    /**
     * Mengambil judul chapter dari dokumen HTML
     * 
     * @param doc Dokumen HTML halaman chapter
     * @return Judul chapter atau string kosong jika tidak ditemukan
     */
    override suspend fun getChapterTitle(doc: Document): String =
        withContext(Dispatchers.Default) {
            doc.selectFirst(SELECTOR_CHAPTER_TITLE)?.text().orEmpty()
        }

    /**
     * Mengambil konten teks chapter dari dokumen HTML
     * 
     * @param doc Dokumen HTML halaman chapter
     * @return Konten chapter dalam format teks
     * @throws NoSuchElementException jika selector chapter text tidak ditemukan
     */
    override suspend fun getChapterText(doc: Document): String =
        withContext(Dispatchers.Default) {
            val element = doc.selectFirst(SELECTOR_CHAPTER_TEXT)
                ?: throw NoSuchElementException("Chapter text element not found")
            TextExtractor.get(element)
        }

    /**
     * Mengambil URL cover image dari halaman detail buku
     * 
     * @param bookUrl URL halaman detail buku
     * @return Response berisi URL cover image atau null
     */
    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> =
        withContext(Dispatchers.Default) {
            tryConnect {
                networkClient
                    .get(bookUrl)
                    .toDocument()
                    .selectFirst(SELECTOR_BOOK_COVER_META)
                    ?.attr("content")
            }
        }

    /**
     * Mengambil deskripsi buku dari halaman detail
     * 
     * @param bookUrl URL halaman detail buku
     * @return Response berisi deskripsi buku atau null
     */
    override suspend fun getBookDescription(bookUrl: String): Response<String?> =
        withContext(Dispatchers.Default) {
            tryConnect {
                networkClient
                    .get(bookUrl)
                    .toDocument()
                    .selectFirst(SELECTOR_BOOK_DESCRIPTION)
                    ?.text()
            }
        }

    /**
     * Mengambil daftar chapter dari buku
     * 
     * Proses:
     * 1. Ambil novel ID dari meta tag og:url
     * 2. Request AJAX ke endpoint chapter-archive
     * 3. Parse response HTML untuk mendapatkan daftar chapter
     * 
     * @param bookUrl URL halaman detail buku
     * @return Response berisi list ChapterResult
     */
    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> =
        withContext(Dispatchers.Default) {
            tryConnect {
                // Ekstrak novel ID dari URL
                val novelId = extractNovelId(bookUrl)
                
                // Build request untuk chapter list
                val chapterListUrl = buildChapterListUrl(novelId)
                val headers = buildChapterListHeaders(bookUrl)
                val request = getRequest(chapterListUrl, headers)
                
                // Parse chapter list
                networkClient.call(request)
                    .toDocument()
                    .select(SELECTOR_CHAPTER_LIST)
                    .map { parseChapterElement(it) }
            }
        }

    /**
     * Mengekstrak novel ID dari URL buku
     * 
     * @param bookUrl URL halaman detail buku
     * @return Novel ID
     * @throws NoSuchElementException jika meta tag tidak ditemukan
     * @throws IllegalStateException jika path segment kosong
     */
    private suspend fun extractNovelId(bookUrl: String): String {
        val doc = networkClient.get(bookUrl).toDocument()
        val ogUrl = doc.expectFirst(SELECTOR_OG_URL).attr("content")
        
        return ogUrl.toUrlBuilderSafe()
            .build()
            .lastPathSegment
            ?: throw IllegalStateException("Novel ID not found in URL")
    }

    /**
     * Membuat URL untuk AJAX request chapter list
     * 
     * @param novelId ID novel
     * @return URL lengkap untuk chapter archive endpoint
     */
    private fun buildChapterListUrl(novelId: String): String =
        baseUrl.toUrlBuilderSafe()
            .addPath("ajax", "chapter-archive")
            .add("novelId" to novelId)
            .toString()

    /**
     * Membuat headers untuk request chapter list
     * 
     * @param bookUrl URL buku untuk Referer header
     * @return Headers object
     */
    private fun buildChapterListHeaders(bookUrl: String): Headers =
        Headers.Builder()
            .add("Accept", "*/*")
            .add("X-Requested-With", "XMLHttpRequest")
            .add("User-Agent", USER_AGENT)
            .add("Referer", "$bookUrl#tab-chapters-title")
            .build()

    /**
     * Parsing element chapter menjadi ChapterResult
     * 
     * @param element Element HTML chapter link
     * @return ChapterResult
     */
    private fun parseChapterElement(element: Element): ChapterResult =
        ChapterResult(
            title = element.attr("title").orEmpty(),
            url = element.attr("href").orEmpty()
        )

    /**
     * Mengambil katalog buku dengan pagination
     * 
     * @param index Index halaman (0-based)
     * @return Response berisi PagedList dari BookResult
     */
    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> =
        withContext(Dispatchers.Default) {
            val page = index + 1
            val url = catalogUrl.toUrlBuilderSafe()
                .ifCase(page > 1) { add("page", page.toString()) }
                .toString()
            
            getPagesList(index, url)
        }

    /**
     * Mencari buku berdasarkan keyword dengan pagination
     * 
     * @param index Index halaman (0-based)
     * @param input Keyword pencarian
     * @return Response berisi PagedList dari BookResult
     */
    override suspend fun getCatalogSearch(
        index: Int,
        input: String,
    ): Response<PagedList<BookResult>> =
        withContext(Dispatchers.Default) {
            val page = index + 1
            val url = baseUrl.toUrlBuilderSafe()
                .addPath("search")
                .add("keyword" to input)
                .ifCase(page > 1) { add("page", page.toString()) }
                .toString()
            
            getPagesList(index, url)
        }
}