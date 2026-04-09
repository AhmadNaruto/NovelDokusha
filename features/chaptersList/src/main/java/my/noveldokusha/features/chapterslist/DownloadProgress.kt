package my.noveldokusha.features.chapterslist

data class DownloadProgress(
    val isDownloading: Boolean = false,
    val total: Int = 0,
    val completed: Int = 0,
    val failed: Int = 0,
    val currentChapterTitle: String? = null,
    val failedChapters: List<String> = emptyList(),
) {
    val percentage: Int
        get() = if (total == 0) 0 else (completed * 100) / total

    val isComplete: Boolean
        get() = !isDownloading && (completed + failed) >= total && total > 0
}
