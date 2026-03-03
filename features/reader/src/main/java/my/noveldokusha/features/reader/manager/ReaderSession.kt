package my.noveldokusha.features.reader.manager

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.noveldoksuha.data.AppRepository
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.features.reader.ReaderRepository
import my.noveldokusha.features.reader.domain.ChapterLoaded
import my.noveldokusha.features.reader.domain.ChapterState
import my.noveldokusha.features.reader.domain.ReaderItem
import my.noveldokusha.features.reader.domain.ReaderState
import my.noveldokusha.features.reader.domain.ReadingChapterPosStats
import my.noveldokusha.features.reader.domain.chapterReadPercentage
import my.noveldokusha.features.reader.features.ReaderChaptersLoader
import my.noveldokusha.features.reader.features.ReaderLiveTranslation
import my.noveldokusha.features.reader.tools.ChaptersIsReadRoutine
import my.noveldokusha.features.reader.ui.ReaderViewHandlersActions
import my.noveldokusha.text_translator.domain.TranslationManager
import my.noveldokusha.feature.local_database.DAOs.ChapterTranslationDao
import my.noveldokusha.feature.local_database.tables.Chapter
import kotlin.properties.Delegates


internal class ReaderSession(
    val bookUrl: String,
    initialChapterUrl: String,
    private val appRepository: AppRepository,
    private val appPreferences: AppPreferences,
    private val readerRepository: ReaderRepository,
    readerViewHandlersActions: ReaderViewHandlersActions,
    @ApplicationContext private val context: Context,
    translationManager: TranslationManager,
    private val chapterTranslationDao: ChapterTranslationDao,
) {
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("ReaderSession")
    )

    private var chapterUrl: String = initialChapterUrl

    private val readRoutine = ChaptersIsReadRoutine(appRepository)
    private val orderedChapters = mutableListOf<Chapter>()
    
    // Track which chapter index has been triggered for pre-translation to avoid duplicates
    private var lastPreTranslatedChapterIndex: Int = -1

    var bookTitle: String? = null
    private var bookCoverUrl: String? = null

    var currentChapter: ChapterState by Delegates.observable(
        ChapterState(
            chapterUrl = chapterUrl,
            chapterItemPosition = 0,
            offset = 0
        )
    ) { _, old, new ->
        chapterUrl = new.chapterUrl
        if (old.chapterUrl != new.chapterUrl) {
            readerRepository.saveBookLastReadPositionState(bookUrl, new, old)
        }
    }

    val readingStats = mutableStateOf<ReadingChapterPosStats?>(null)
    val readingChapterProgressPercentage = derivedStateOf {
        readingStats.value?.chapterReadPercentage() ?: 0f
    }

    val readerLiveTranslation = ReaderLiveTranslation(
        translationManager = translationManager,
        appPreferences = appPreferences,
        chapterTranslationDao = chapterTranslationDao
    )

    val readerChaptersLoader = ReaderChaptersLoader(
        readerRepository = readerRepository,
        translatorTranslateOrNull = { readerLiveTranslation.translatorState?.translate?.invoke(it) },
        translatorIsActive = { readerLiveTranslation.translatorState != null },
        translatorSourceLanguageOrNull = { readerLiveTranslation.translatorState?.sourceLocale?.displayLanguage },
        translatorTargetLanguageOrNull = { readerLiveTranslation.translatorState?.targetLocale?.displayLanguage },
        translatorProvider = { if (readerLiveTranslation.isUsingGemini()) "gemini" else "google" },
        translatorBatchTranslateOrNull = { readerLiveTranslation.getBatchTranslator() },
        bookUrl = bookUrl,
        orderedChapters = orderedChapters,
        readerState = ReaderState.INITIAL_LOAD,
        readerViewHandlersActions = readerViewHandlersActions,
        chapterTranslationDao = chapterTranslationDao,
    ).also {
        // Connect the translation refresh callback to clear chapter cache
        readerLiveTranslation.onClearChapterCache = { it.clearTranslationCache() }
    }

    val items = readerChaptersLoader.getItems()

    fun init() {
        initLoadData()
        scope.launch {
            appRepository.libraryBooks.updateLastReadEpochTimeMilli(
                bookUrl,
                System.currentTimeMillis()
            )
        }
    }

    private fun initLoadData() {
        scope.launch {
            val book = async(Dispatchers.IO) { appRepository.libraryBooks.get(bookUrl) }
            val chapter = async(Dispatchers.IO) { appRepository.bookChapters.get(chapterUrl) }
            val loadTranslator = async(Dispatchers.IO) { readerLiveTranslation.init() }
            val chaptersList = async(Dispatchers.Default) {
                orderedChapters.also { it.addAll(appRepository.bookChapters.chapters(bookUrl)) }
            }
            val chapterIndex = async(Dispatchers.Default) {
                chaptersList.await().indexOfFirst { it.url == chapterUrl }
            }
            chaptersList.await()
            loadTranslator.await()
            bookCoverUrl = book.await()?.coverImageUrl
            bookTitle = book.await()?.title
            currentChapter = ChapterState(
                chapterUrl = chapterUrl,
                chapterItemPosition = chapter.await()?.lastReadPosition ?: 0,
                offset = chapter.await()?.lastReadOffset ?: 0,
            )
            // All data prepared! Let's load the current chapter
            readerChaptersLoader.tryLoadInitial(chapterIndex = chapterIndex.await())
        }
    }

    fun reloadReader() {
        readerChaptersLoader.reload()
    }

    fun updateInfoViewTo(itemIndex: Int) {
        val stats = readerChaptersLoader.getItemContext(
            itemIndex = itemIndex,
            chapterUrl = chapterUrl
        ) ?: return
        readingStats.value = stats
        
        // Trigger pre-fetch and pre-translation based on reading progress
        val progress = stats.chapterReadPercentage()
        val chapterIndex = stats.chapterIndex
        
        // Pre-fetch next chapter at 90% (even without translation)
        if (progress >= 0.90f && !readerChaptersLoader.isLastChapter(chapterIndex)) {
            val nextChapterIndex = chapterIndex + 1
            if (!readerChaptersLoader.isChapterIndexLoaded(nextChapterIndex)) {
                scope.launch {
                    readerChaptersLoader.tryLoadNext()
                }
            }
        }
        
        // Pre-translate next chapter at 80% (only with translation enabled)
        // Only trigger once per chapter to avoid duplicate API calls
        if (progress >= 0.80f && 
            readerLiveTranslation.translatorState != null && 
            chapterIndex != lastPreTranslatedChapterIndex) {
            lastPreTranslatedChapterIndex = chapterIndex
            scope.launch {
                readerChaptersLoader.preTranslateNextChapter(chapterIndex)
            }
        }
    }

    fun markChapterStartAsSeen(chapterUrl: String) {
        readRoutine.setReadStart(chapterUrl = chapterUrl)
    }

    fun markChapterEndAsSeen(chapterUrl: String) {
        readRoutine.setReadEnd(chapterUrl = chapterUrl)
    }

    fun saveCurrentPosition(currentChapter: ChapterState) {
        readerRepository.saveBookLastReadPositionState(
            bookUrl = bookUrl,
            newChapter = currentChapter
        )
    }

    fun close() {
        scope.coroutineContext.cancelChildren()
    }
}
