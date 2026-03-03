package my.noveldokusha.scraper

/**
 * Represents a chapter ready for download with its content and title.
 */
data class ChapterDownload(
    val content: String,
    val title: String?
)
