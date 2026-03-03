package my.noveldokusha.scraper.domain

import kotlin.ConsistentCopyVisibility

@ConsistentCopyVisibility
data class ChapterResult internal constructor(
    val title: String,
    val url: String
)