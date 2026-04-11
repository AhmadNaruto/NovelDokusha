package my.noveldokusha.features.reader.manager

import my.noveldoksuha.data.AppRepository
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.features.reader.ReaderRepository
import my.noveldokusha.features.reader.ui.ReaderViewHandlersActions
import my.noveldokusha.feature.local_database.DAOs.ChapterTranslationDao
import my.noveldokusha.text_translator.domain.TranslationManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ReaderSessionProvider @Inject constructor(
    private val appRepository: AppRepository,
    private val appPreferences: AppPreferences,
    private val translationManager: TranslationManager,
    private val readerRepository: ReaderRepository,
    private val readerViewHandlersActions: ReaderViewHandlersActions,
    private val chapterTranslationDao: ChapterTranslationDao,
) {
    fun create(
        bookUrl: String,
        initialChapterUrl: String,
    ): ReaderSession = ReaderSession(
        bookUrl = bookUrl,
        initialChapterUrl = initialChapterUrl,
        appRepository = appRepository,
        translationManager = translationManager,
        appPreferences = appPreferences,
        readerRepository = readerRepository,
        readerViewHandlersActions = readerViewHandlersActions,
        chapterTranslationDao = chapterTranslationDao,
    )
}
