package my.noveldokusha.scraper

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import my.noveldokusha.BookMetadata
import my.noveldokusha.ChapterMetadata
import my.noveldokusha.DataCache_DatabaseSearchGenres
import my.noveldokusha.bookstore
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.TextNode
import java.io.PrintWriter
import java.io.StringWriter
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.util.*

fun String.urlEncode(): String = URLEncoder.encode(this, "utf-8")

suspend fun Connection.getIO(): Document = withContext(Dispatchers.IO) { get() }
suspend fun Connection.postIO(): Document = withContext(Dispatchers.IO) { post() }
suspend fun Connection.executeIO(): Connection.Response = withContext(Dispatchers.IO) { execute() }
suspend fun String.urlEncodeAsync(): String = withContext(Dispatchers.IO) { this@urlEncodeAsync.urlEncode() }

fun Connection.addUserAgent(): Connection =
	this.userAgent("Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)")

fun Connection.addHeaderRequest() = this.header("x-requested-with", "XMLHttpRequest")!!

sealed class Response<T>
{
	class Success<T>(val data: T) : Response<T>()
	class Error<T>(val message: String) : Response<T>()
}

object scrubber
{
	fun getCompatibleSource(url: String): source_interface? = sourcesList.find { url.startsWith(it.baseUrl) }
	fun getCompatibleSourceCatalog(url: String): source_interface.catalog? = sourcesListCatalog.find { url.startsWith(it.baseUrl) }
	fun getCompatibleDatabase(url: String): database_interface? = databasesList.find { url.startsWith(it.baseUrl) }
	
	interface source_interface
	{
		val name: String
		val baseUrl: String
		
		// Transform current url to preferred url
		fun transformChapterUrl(url: String): String = url
		
		suspend fun getChapterText(doc: Document): String
		
		interface base : source_interface
		interface catalog : source_interface
		{
			val catalogUrl: String
			
			suspend fun getChapterList(doc: Document): List<ChapterMetadata>
			suspend fun getCatalogList(index: Int): Response<List<BookMetadata>>
			suspend fun getCatalogSearch(index: Int, input: String): Response<List<BookMetadata>>
		}
	}
	
	interface database_interface
	{
		val id: String
		val name: String
		val baseUrl: String
		
		val searchGenresCache get() = DataCache_DatabaseSearchGenres(id)
		
		suspend fun getSearchGenres(): Response<Map<String, String>>
		suspend fun getSearch(index: Int, input: String): Response<List<BookMetadata>>
		suspend fun getSearchAdvanced(
			index: Int,
			genresIncludedId: List<String>,
			genresExcludedId: List<String>
		): Response<List<BookMetadata>>
		
		data class BookAuthor(val name: String, val url: String?)
		data class BookData(
			val title: String,
			val description: String,
			val alternativeTitles: List<String>,
			val authors: List<BookAuthor>,
			val tags: List<String>,
			val genres: List<String>,
			val bookType: String,
			val relatedBooks: List<BookMetadata>,
			val similarRecommended: List<BookMetadata>
		)
		
		fun getBookData(doc: Document): BookData
	}
	
	fun getNodeTextTransversal(node: org.jsoup.nodes.Node): List<String>
	{
		if (node is TextNode)
		{
			val text = node.text().trim()
			return if (text.isEmpty()) listOf() else listOf(text)
		}
		return node.childNodes().flatMap { childNode -> getNodeTextTransversal(childNode) }
	}
	
	val sourcesList = setOf(
		my.noveldokusha.scraper.sources.LightNovelsTranslations(),
		my.noveldokusha.scraper.sources.ReadLightNovel(),
		my.noveldokusha.scraper.sources.ReadNovelFull(),
		my.noveldokusha.scraper.sources.DivineDaoLibrary(),
		my.noveldokusha.scraper.sources.NovelUpdates(),
		my.noveldokusha.scraper.sources.Reddit(),
		my.noveldokusha.scraper.sources.Hoopla2017(),
	)
	
	val sourcesListCatalog = sourcesList.filterIsInstance<source_interface.catalog>().toSet()
	
	val databasesList = setOf(
		my.noveldokusha.scraper.databases.NovelUpdates(),
		my.noveldokusha.scraper.databases.BakaUpdates()
	)
}

suspend fun downloadChapter(chapterUrl: String): Response<String>
{
	return tryConnect {
		val con = Jsoup.connect(chapterUrl)
			.addUserAgent()
			.followRedirects(true)
			.timeout(2 * 60 * 1000)
			.referrer("http://www.google.com")
			.header("Content-Language", "en-US")
			.executeIO()
		
		val realUrl = con.url().toString()
		
		val error by lazy {
			"""
				Unable to load chapter from url:
				$chapterUrl
				
				Redirect url:
				$realUrl
				
				Source not supported
			""".trimIndent()
		}
		
		val source = scrubber.getCompatibleSource(realUrl) ?: return@tryConnect { Response.Error<String>(error) }()
		
		val doc = fetchDoc(source.transformChapterUrl(realUrl))
		val body = source.getChapterText(doc)
		Response.Success(body)
	}
}

suspend fun fetchChaptersList(bookUrl: String, tryCache: Boolean = true): Response<List<bookstore.Chapter>>
{
	if (tryCache) bookstore.bookChapter.chapters(bookUrl).let {
		if (it.isNotEmpty()) return Response.Success(it)
	}
	
	val error by lazy {
		"""
			Incompatible source.
			
			Can't find compatible source for:
			$bookUrl
		""".trimIndent()
	}
	
	// Return if can't find compatible scrubber for url
	val scrap = scrubber.getCompatibleSourceCatalog(bookUrl) ?: return Response.Error(error)
	
	return tryConnect {
		val doc = fetchDoc(bookUrl)
		scrap.getChapterList(doc)
			.map { bookstore.Chapter(title = it.title, url = it.url, bookUrl = bookUrl) }
			.let {
				bookstore.bookChapter.insert(it)
				Response.Success(bookstore.bookChapter.chapters(bookUrl))
			}
	}
}

suspend fun <T> tryConnect(extraErrorInfo: String = "", call: suspend () -> Response<T>): Response<T> = try
{
	call()
}
catch (e: SocketTimeoutException)
{
	val error = """
		Timeout error.

		Info:
		${extraErrorInfo.ifBlank { "No info" }}
		
		Message:
		${e.message}
	""".trimIndent()
	Response.Error(error)
}
catch (e: Exception)
{
	val stacktrace = StringWriter().apply { e.printStackTrace(PrintWriter(this)) }
	val error = """
		Unknown error.
		
		Info:
		${extraErrorInfo.ifBlank { "No Info" }}
		
		Message:
		${e.message}

		Stacktrace:
		$stacktrace
	""".trimIndent()
	Response.Error(error)
}

suspend fun fetchDoc(url: String, timeoutMilliseconds: Int = 2 * 60 * 1000): Document
{
	return Jsoup.connect(url)
		.timeout(timeoutMilliseconds)
		.addUserAgent()
		.referrer("http://www.google.com")
		.header("Content-Language", "en-US")
		.header("Accept", "text/html")
		.header("Accept-Encoding", "gzip,deflate")
		.getIO()
}

class BooksFetchIterator(
	private val coroutineScope: CoroutineScope,
	private var fn: (suspend (index: Int) -> Response<List<BookMetadata>>)
)
{
	enum class STATE
	{ IDLE, LOADING, CONSUMED }
	
	private var state = STATE.IDLE
	private var booksCount: Int = 0
	private var index = 0
	private var job: Job? = null
	
	val onSuccess = MutableLiveData<Response.Success<List<BookMetadata>>>()
	val onCompleted = MutableLiveData<Unit>()
	val onCompletedEmpty = MutableLiveData<Unit>()
	val onError = MutableLiveData<Response.Error<List<BookMetadata>>>()
	val onFetching = MutableLiveData<Boolean>()
	val onReset = MutableLiveData<Unit>()
	
	fun setFunction(fn: (suspend (index: Int) -> Response<List<BookMetadata>>))
	{
		this.fn = fn
	}
	
	fun reset()
	{
		state = STATE.IDLE
		booksCount = 0
		index = 0
		job?.cancel()
		onReset.value = Unit
	}
	
	fun removeAllObservers(owner: LifecycleOwner)
	{
		listOf(onSuccess, onCompleted, onCompletedEmpty, onError, onFetching).forEach {
			it.removeObservers(owner)
		}
	}
	
	fun fetchTrigger(trigger: () -> Boolean)
	{
		if (state == STATE.IDLE && trigger())
			fetchNext()
	}
	
	fun fetchNext()
	{
		if (state != STATE.IDLE) return
		state = STATE.LOADING
		
		job = coroutineScope.launch(Dispatchers.Main) {
			onFetching.value = true
			val res = withContext(Dispatchers.IO) { fn(index) }
			onFetching.value = false
			if (!isActive) return@launch
			when (res)
			{
				is Response.Success ->
				{
					if (res.data.isEmpty())
					{
						state = STATE.CONSUMED
						if (booksCount == 0)
							onCompletedEmpty.value = Unit
						else
							onCompleted.value = Unit
					}
					else
					{
						state = STATE.IDLE
						booksCount += res.data.size
						onSuccess.value = res
					}
				}
				is Response.Error ->
				{
					state = STATE.CONSUMED
					onError.value = res
					if (booksCount == 0)
						onCompletedEmpty.value = Unit
					else
						onCompleted.value = Unit
				}
			}
			index += 1
		}
	}
}