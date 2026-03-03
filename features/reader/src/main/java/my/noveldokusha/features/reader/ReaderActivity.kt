package my.noveldokusha.features.reader

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.noveldoksuha.coreui.BaseActivity
import my.noveldoksuha.coreui.composableActions.SetSystemBarTransparent
import my.noveldoksuha.coreui.mappers.toPreferenceTheme
import my.noveldoksuha.coreui.theme.Theme
import my.noveldoksuha.coreui.theme.colorAttrRes
import my.noveldokusha.core.utils.Extra_Boolean
import my.noveldokusha.core.utils.Extra_String
import my.noveldokusha.features.reader.domain.ChapterState
import my.noveldokusha.features.reader.domain.ReaderItem
import my.noveldokusha.features.reader.domain.ReaderState
import my.noveldokusha.features.reader.domain.indexOfReaderItem
import my.noveldokusha.features.reader.tools.FontsLoader
import my.noveldokusha.features.reader.ui.ReaderScreen
import my.noveldokusha.features.reader.ui.ReaderViewHandlersActions
import my.noveldokusha.features.reader.ui.components.readerItems
import my.noveldokusha.navigation.NavigationRoutes
import my.noveldokusha.reader.R
import javax.inject.Inject

@AndroidEntryPoint
class ReaderActivity : BaseActivity() {
    class IntentData : Intent, ReaderStateBundle {
        override var bookUrl by Extra_String()
        override var chapterUrl by Extra_String()
        override var introScrollToSpeaker by Extra_Boolean()

        constructor(intent: Intent) : super(intent)
        constructor(
            ctx: Context,
            bookUrl: String,
            chapterUrl: String,
            scrollToSpeakingItem: Boolean = false
        ) : super(
            ctx,
            ReaderActivity::class.java
        ) {
            this.bookUrl = bookUrl
            this.chapterUrl = chapterUrl
            this.introScrollToSpeaker = scrollToSpeakingItem
        }
    }

    @Inject
    lateinit var navigationRoutes: NavigationRoutes

    @Inject
    internal lateinit var readerViewHandlersActions: ReaderViewHandlersActions

    private var listIsScrolling = false
    private val fadeInTextLiveData = MutableLiveData(false)

    private val viewModel by viewModels<ReaderViewModel>()

    private val fontsLoader = FontsLoader()

    override fun onBackPressed() {
        viewModel.onCloseManually()
        super.onBackPressed()
    }

    override fun onDestroy() {
        readerViewHandlersActions.invalidate()
        super.onDestroy()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fadeInTextLiveData.distinctUntilChanged().observe(this) {
            // Fade-in animation is now handled by Compose
        }

        lifecycleScope.launch {
            viewModel.onTranslatorChanged.collect {
                viewModel.reloadReader()
            }
        }

        readerViewHandlersActions.forceUpdateListViewState = {
            withContext(Dispatchers.Main.immediate) {
                // In Compose, state updates are automatic
            }
        }

        readerViewHandlersActions.maintainStartPosition = {
            withContext(Dispatchers.Main.immediate) {
                it()
                // Position maintenance is handled by LazyListState
            }
        }

        readerViewHandlersActions.setInitialPosition = {
            withContext(Dispatchers.Main.immediate) {
                initialScrollToChapterItemPosition(
                    chapterIndex = it.chapterIndex,
                    chapterItemPosition = it.chapterItemPosition,
                    offset = it.chapterItemOffset
                )
            }
        }

        readerViewHandlersActions.maintainLastVisiblePosition = {
            withContext(Dispatchers.Main.immediate) {
                it()
                // Position maintenance is handled by LazyListState
            }
        }

        // Set current screen to be kept bright always or not
        snapshotFlow { viewModel.state.settings.keepScreenOn.value }
            .asLiveData()
            .observe(this) { keepScreenOn ->
                val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                if (keepScreenOn) window.addFlags(flag) else window.clearFlags(flag)
            }

        setContent {
            Theme(themeProvider) {
                SetSystemBarTransparent()

                // Reader info
                ReaderScreen(
                    state = viewModel.state,
                    onTextFontChanged = { appPreferences.READER_FONT_FAMILY.value = it },
                    onTextSizeChanged = { appPreferences.READER_FONT_SIZE.value = it },
                    onSelectableTextChange = { appPreferences.READER_SELECTABLE_TEXT.value = it },
                    onKeepScreenOn = { appPreferences.READER_KEEP_SCREEN_ON.value = it },
                    onFollowSystem = { appPreferences.THEME_FOLLOW_SYSTEM.value = it },
                    onThemeSelected = { appPreferences.THEME_ID.value = it.toPreferenceTheme },
                    onFullScreen = { appPreferences.READER_FULL_SCREEN.value = it },
                    onPressBack = {
                        viewModel.onCloseManually()
                        finish()
                    },
                    onOpenChapterInWeb = {
                        val url = viewModel.state.readerInfo.chapterUrl.value
                        if (url.isNotBlank()) {
                            navigationRoutes.webView(this, url = url).let(::startActivity)
                        }
                    },
                    readerContent = { paddingValues ->
                        val listState = rememberLazyListState()
                        val scope = rememberCoroutineScope()
                        var hasInitialLoad by remember { mutableStateOf(false) }

                        // Get current font settings
                        val currentTypeface = fontsLoader.getTypeFaceNORMAL(appPreferences.READER_FONT_FAMILY.value)
                        val currentTypefaceBold = fontsLoader.getTypeFaceBOLD(appPreferences.READER_FONT_FAMILY.value)
                        val currentFontSize = appPreferences.READER_FONT_SIZE.value
                        val currentTextSelectability = appPreferences.READER_SELECTABLE_TEXT.value

                        // Track first and last visible items for chapter loading
                        LaunchedEffect(listState) {
                            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                                .collect { visibleItems ->
                                    if (visibleItems.isNotEmpty()) {
                                        val firstVisibleItemIndex = visibleItems.first().index
                                        updateCurrentReadingPosSavingState(
                                            firstVisibleItemIndex = firstVisibleItemIndex
                                        )
                                        updateInfoView(firstVisibleItemIndex)
                                        updateReadingState(
                                            firstVisibleItem = visibleItems.first().index,
                                            lastVisibleItem = visibleItems.last().index,
                                            totalItemCount = viewModel.items.size
                                        )
                                    }
                                }
                        }

                        // Handle scroll state changes
                        LaunchedEffect(listState) {
                            snapshotFlow { listState.isScrollInProgress }
                                .collect { isScrolling ->
                                    listIsScrolling = isScrolling
                                }
                        }

                        // Apply fade-in animation on initial load
                        LaunchedEffect(Unit) {
                            delay(200)
                            hasInitialLoad = true
                        }

                        // Handle initial scroll position
                        LaunchedEffect(viewModel.items.size) {
                            if (viewModel.items.isNotEmpty() && !hasInitialLoad) {
                                // Check for title index to scroll to
                                val titleIndex = viewModel.items.indexOfFirst { it is ReaderItem.Title }
                                if (titleIndex != -1) {
                                    listState.scrollToItem(titleIndex)
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        viewModel.state.showReaderInfo.value =
                                            !viewModel.state.showReaderInfo.value
                                    })
                                }
                        ) {
                            readerItems(
                                items = viewModel.items,
                                bookUrl = viewModel.bookUrl,
                                textSelectability = currentTextSelectability,
                                fontSize = currentFontSize,
                                typeface = currentTypeface,
                                typefaceBold = currentTypefaceBold,
                                onChapterStartVisible = viewModel::markChapterStartAsSeen,
                                onChapterEndVisible = viewModel::markChapterEndAsSeen,
                                onReloadReader = viewModel::reloadReader,
                                onClick = {
                                    viewModel.state.showReaderInfo.value =
                                        !viewModel.state.showReaderInfo.value
                                }
                            )
                        }

                        if (viewModel.state.showInvalidChapterDialog.value) {
                            BasicAlertDialog(onDismissRequest = {
                                viewModel.state.showInvalidChapterDialog.value = false
                            }) {
                                Text(stringResource(id = R.string.invalid_chapter))
                            }
                        }
                    }
                )
            }
        }

        snapshotFlow { viewModel.state.settings.fullScreen.value }
            .asLiveData()
            .observe(this) { fullscreen ->
                when {
                    fullscreen -> setupFullScreenMode()
                    else -> setupNormalScreenMode()
                }
            }
        setupSystemBarAppearance()

        lifecycleScope.launch {
            delay(200)
            fadeInTextLiveData.postValue(true)
        }

        // Use case: user opens reader on the same book, on the same chapter url (session is maintained)
        if (readerViewHandlersActions.introScrollToCurrentChapter) {
            readerViewHandlersActions.introScrollToCurrentChapter = false
            val chapterState = viewModel.readingCurrentChapter
            val chapterStats =
                viewModel.chaptersLoader.chaptersStats[chapterState.chapterUrl] ?: return
            initialScrollToChapterItemPosition(
                chapterIndex = chapterStats.orderedChaptersIndex,
                chapterItemPosition = chapterState.chapterItemPosition,
                offset = chapterState.offset
            )
        }
    }

    private fun setupNormalScreenMode() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.displayCutout())
        controller.show(WindowInsetsCompat.Type.systemBars())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.statusBarColor = R.attr.colorSurface.colorAttrRes(this)
    }

    private fun setupFullScreenMode() {
        enableEdgeToEdge()
        // Fullscreen mode that ignores any cutout, notch etc.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.displayCutout())
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.statusBarColor = R.attr.colorSurface.colorAttrRes(this)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    }

    private fun setupSystemBarAppearance() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        combine(
            snapshotFlow { viewModel.state.showReaderInfo.value },
            snapshotFlow { viewModel.state.settings.fullScreen.value }
        ) { showReaderInfo, fullScreen -> showReaderInfo to fullScreen }
            .distinctUntilChangedBy { (showReaderInfo, fullScreen) -> showReaderInfo || !fullScreen }
            .asLiveData().observe(this) { (showReaderInfo, fullScreen) ->
                val show = showReaderInfo || !fullScreen
                when {
                    show -> controller.show(WindowInsetsCompat.Type.statusBars())
                    else -> controller.hide(WindowInsetsCompat.Type.statusBars())
                }
            }
    }

    private fun updateReadingState(firstVisibleItem: Int, lastVisibleItem: Int, totalItemCount: Int) {
        val visibleItemCount =
            if (totalItemCount == 0) 0 else (lastVisibleItem - firstVisibleItem + 1)

        val isTop = visibleItemCount != 0 && firstVisibleItem <= 1
        val isBottom =
            visibleItemCount != 0 && (firstVisibleItem + visibleItemCount) >= totalItemCount - 1

        when (viewModel.chaptersLoader.readerState) {
            ReaderState.IDLE -> {
                if (isBottom) {
                    viewModel.chaptersLoader.tryLoadNext()
                }
                if (isTop) {
                    viewModel.chaptersLoader.tryLoadPrevious()
                }
            }
            ReaderState.LOADING -> run {}
            ReaderState.INITIAL_LOAD -> run {}
        }
    }

    private fun initialScrollToChapterItemPosition(
        chapterIndex: Int,
        chapterItemPosition: Int,
        offset: Int
    ) {
        val index = indexOfReaderItem(
            list = viewModel.items,
            chapterIndex = chapterIndex,
            chapterItemPosition = chapterItemPosition
        )
        if (index != -1) {
            // Scroll position will be handled by LazyListState in Compose
        }
        fadeInTextLiveData.postValue(true)
    }

    private fun updateInfoView(firstVisibleItemIndex: Int) {
        viewModel.updateInfoViewTo(firstVisibleItemIndex)
    }

    override fun onPause() {
        // Explicitly save to database when app pauses
        viewModel.saveCurrentReadingPosition()
        super.onPause()
    }

    private fun updateCurrentReadingPosSavingState(firstVisibleItemIndex: Int) {
        val item = viewModel.items.getOrNull(firstVisibleItemIndex) ?: return
        if (item !is ReaderItem.Position) return

        // In Compose, offset is handled differently - using the item index directly
        viewModel.readingCurrentChapter = ChapterState(
            chapterUrl = item.chapterUrl,
            chapterItemPosition = item.chapterItemPosition,
            offset = 0 // Offset handling is different in LazyColumn
        )
    }
}
