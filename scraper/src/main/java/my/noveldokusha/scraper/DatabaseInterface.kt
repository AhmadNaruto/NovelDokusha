package my.noveldokusha.scraper

import androidx.annotation.StringRes
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.scraper.domain.BookResult

/**
 * Represents a genre filter for database searches.
 */
data class SearchGenre(
    val id: String,
    val displayName: String,
    // Deprecated for backward compatibility
    @Deprecated("Use displayName", ReplaceWith("displayName"))
    val genreName: String = displayName
)

/**
 * Interface defining a novel database.
 */
interface DatabaseInterface {
    /**
     * Unique identifier for this database.
     */
    val id: String

    /**
     * String resource ID for the database name.
     */
    @get:StringRes
    val nameStrId: Int

    /**
     * Base URL of the database.
     */
    val baseUrl: String

    /**
     * URL to the database icon.
     */
    val iconUrl: String get() = "$baseUrl/favicon.ico"

    /**
     * Cache file name for search genres.
     */
    val searchGenresCacheFileName: String get() = "database_search_genres_v2_$id"

    /**
     * Fetches a page from the catalog.
     */
    suspend fun getCatalog(index: Int): Response<PagedList<BookResult>>

    /**
     * Searches books by title.
     */
    suspend fun searchByTitle(index: Int, query: String): Response<PagedList<BookResult>>

    /**
     * Searches books by genre filters.
     * @param index Zero-based page index.
     * @param genresIncludedId Genre IDs that must be included.
     * @param genresExcludedId Genre IDs that must be excluded.
     */
    suspend fun searchByFilters(
        index: Int,
        genresIncludedId: List<String>,
        genresExcludedId: List<String>
    ): Response<PagedList<BookResult>>

    /**
     * Fetches the list of available search genres.
     */
    suspend fun getSearchFilters(): Response<List<SearchGenre>>

    /**
     * Fetches detailed book data.
     */
    suspend fun getBookData(bookUrl: String): Response<DatabaseInterface.BookData>

    /**
     * Fetches author data.
     */
    suspend fun getAuthorData(authorUrl: String): Response<DatabaseInterface.AuthorData>

    /**
     * Author metadata.
     */
    data class AuthorMetadata(
        val name: String,
        val url: String?
    )

    /**
     * Detailed book information.
     */
    data class BookData(
        val title: String,
        val description: String,
        val alternativeTitles: List<String>,
        val authors: List<AuthorMetadata>,
        val tags: List<String>,
        val genres: List<SearchGenre>,
        val bookType: String,
        val relatedBooks: List<BookResult>,
        val similarRecommended: List<BookResult>,
        val coverImageUrl: String?
    )

    /**
     * Author detailed information.
     */
    data class AuthorData(
        val name: String,
        val coverImageUrl: String? = null,
        val books: List<BookResult>,
        val description: String = "",
        val associatedNames: List<String> = emptyList(),
        val genres: List<String> = emptyList()
    )
}
