package my.noveldokusha.features.reader

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import my.noveldoksuha.coreui.BaseActivity
import my.noveldoksuha.coreui.composableActions.SetSystemBarTransparent
import my.noveldoksuha.coreui.mappers.toPreferenceTheme
import my.noveldoksuha.coreui.theme.Theme
import my.noveldoksuha.coreui.theme.colorAttrRes
import my.noveldokusha.core.utils.Extra_Boolean
import my.noveldokusha.core.utils.Extra_String
import my.noveldokusha.features.reader.domain.ChapterState
import my.noveldokusha.features.reader.domain.ReaderItem
import my.noveldokusha.features.reader.domain.indexOfReaderItem
import my.noveldokusha.features.reader.tools.FontsLoader
import my.noveldokusha.features.reader.ui.ReaderLazyList
import my.noveldokusha.features.reader.ui.ReaderScreen
import my.noveldokusha.features.reader.ui.ReaderViewHandlersActions
import my.noveldokusha.navigation.NavigationRoutes
import my.noveldokusha.reader.R
import javax.inject.Inject

@AndroidEntryPoint
class ReaderActivity : BaseActivity() {
    class IntentData : Intent, ReaderStateBundle {
        override var bookUrl by Extra_String()
        override var chapterUrl by Extra_String()

        constructor(intent: Intent) : super(intent)
        constructor(
            ctx: Context,
            bookUrl: String,
            chapterUrl: String,
        ) : super(
            ctx,
            ReaderActivity::class.java
        ) {
            this.bookUrl = bookUrl
            this.chapterUrl = chapterUrl
        }
    }

    @Inject
    lateinit var navigationRoutes: NavigationRoutes

    @Inject
    internal lateinit var readerViewHandlersActions: ReaderViewHandlersActions

    private val viewModel by viewModels<ReaderViewModel>()
    private val fontsLoader = FontsLoader()

    private var onBackPressedCallback: androidx.activity.OnBackPressedCallback? = null

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java", ReplaceWith("OnBackPressedDispatcher"))
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
        fontsLoader.init(this)

        lifecycleScope.launch {
            viewModel.onTranslatorChanged.collect {
                viewModel.reloadReader()
            }
        }

        // Keep screen on preference
        snapshotFlow { viewModel.state.settings.keepScreenOn.value }
            .asLiveData()
            .observe(this) { keepScreenOn ->
                val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                if (keepScreenOn) window.addFlags(flag) else window.clearFlags(flag)
            }

        setContent {
            Theme(themeProvider) {
                SetSystemBarTransparent()

                val lazyListState = rememberLazyListState()
                val items by rememberUpdatedState(viewModel.items)
                val bookUrl by rememberUpdatedState(viewModel.bookUrl)

                val fontSize by rememberUpdatedState(
                    viewModel.state.settings.style.textSize.value
                )
                val textFont by rememberUpdatedState(
                    viewModel.state.settings.style.textFont.value
                )
                val selectable by rememberUpdatedState(
                    viewModel.state.settings.isTextSelectable.value
                )

                val typeface = remember(textFont) {
                    fontsLoader.getTypeFaceNORMAL(textFont)
                }
                val typefaceBold = remember(textFont) {
                    fontsLoader.getTypeFaceBOLD(textFont)
                }

                // Scroll position saving
                LaunchedEffect(lazyListState) {
                    snapshotFlow { lazyListState.firstVisibleItemIndex }
                        .collect { firstVisibleItemIndex ->
                            val item = items.getOrNull(firstVisibleItemIndex) ?: return@collect
                            if (item !is ReaderItem.Position) return@collect

                            // Approximate offset from first visible item
                            val firstVisibleItemScrollOffset = lazyListState.firstVisibleItemScrollOffset
                            viewModel.readingCurrentChapter = ChapterState(
                                chapterUrl = item.chapterUrl,
                                chapterItemPosition = item.chapterItemPosition,
                                offset = firstVisibleItemScrollOffset
                            )
                        }
                }

                // Infinite scroll: detect top/bottom and load more chapters
                LaunchedEffect(lazyListState, items.size) {
                    snapshotFlow {
                        val layoutInfo = lazyListState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        val firstVisible = lazyListState.firstVisibleItemIndex
                        val visibleItems = layoutInfo.visibleItemsInfo.size
                        val lastVisible = firstVisible + visibleItems

                        val isTop = firstVisible <= 1
                        val isBottom = totalItems > 0 && lastVisible >= totalItems - 1

                        isTop to isBottom
                    }
                        .collect { (isTop, isBottom) ->
                            when (viewModel.chaptersLoader.readerState) {
                                my.noveldokusha.features.reader.domain.ReaderState.IDLE -> {
                                    if (isBottom) viewModel.chaptersLoader.tryLoadNext()
                                    if (isTop) viewModel.chaptersLoader.tryLoadPrevious()
                                }
                                my.noveldokusha.features.reader.domain.ReaderState.LOADING -> {}
                                my.noveldokusha.features.reader.domain.ReaderState.INITIAL_LOAD -> {}
                            }
                        }
                }

                // Update info view based on visible items
                LaunchedEffect(lazyListState) {
                    snapshotFlow {
                        val layoutInfo = lazyListState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        visibleItems.lastOrNull()?.index ?: -1
                    }
                        .collect { lastVisibleIndex ->
                            viewModel.updateInfoViewTo(lastVisibleIndex)
                        }
                }

                // Handle font/typeface changes triggering recomposition
                // (Compose automatically recomposes when fontSize, textFont, or selectable change)

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
                    readerContent = {
                        ReaderLazyList(
                            items = items,
                            bookUrl = bookUrl,
                            fontSize = fontSize,
                            typeface = typeface,
                            typefaceBold = typefaceBold,
                            selectable = selectable,
                            lazyListState = lazyListState,
                            onChapterStartVisible = viewModel::markChapterStartAsSeen,
                            onChapterEndVisible = viewModel::markChapterEndAsSeen,
                            onReloadReader = viewModel::reloadReader,
                            onClick = {
                                viewModel.state.showReaderInfo.value =
                                    !viewModel.state.showReaderInfo.value
                            },
                        )
                    },
                )

                if (viewModel.state.showInvalidChapterDialog.value) {
                    BasicAlertDialog(onDismissRequest = {
                        viewModel.state.showInvalidChapterDialog.value = false
                    }) {
                        Text(stringResource(id = R.string.invalid_chapter))
                    }
                }

                // Fullscreen mode setup
                val fullScreen by rememberUpdatedState(viewModel.state.settings.fullScreen.value)
                LaunchedEffect(fullScreen) {
                    when {
                        fullScreen -> setupFullScreenMode()
                        else -> setupNormalScreenMode()
                    }
                }

                // System bar appearance
                setupSystemBarAppearance()

                // Intro scroll to current chapter
                LaunchedEffect(Unit) {
                    delay(200)
                    if (readerViewHandlersActions.introScrollToCurrentChapter) {
                        readerViewHandlersActions.introScrollToCurrentChapter = false
                        val chapterState = viewModel.readingCurrentChapter
                        val chapterStats =
                            viewModel.chaptersLoader.chaptersStats[chapterState.chapterUrl]
                                ?: return@LaunchedEffect

                        scrollToChapterItem(
                            lazyListState = lazyListState,
                            chapterIndex = chapterStats.orderedChaptersIndex,
                            chapterItemPosition = chapterState.chapterItemPosition,
                            offset = chapterState.offset,
                            items = items,
                        )
                    }
                }

                // Handler: force update list state (notifyDataSetChanged equivalent)
                readerViewHandlersActions.forceUpdateListViewState = {
                    // In Compose, state changes trigger recomposition automatically.
                    // We trigger a recomposition by updating a key.
                }

                // Handler: maintain start position
                readerViewHandlersActions.maintainStartPosition = {
                    lifecycleScope.launch {
                        it()
                        val titleIndex = items.indexOfFirst { item -> item is ReaderItem.Title }
                        if (titleIndex != -1) {
                            lazyListState.scrollToItem(titleIndex)
                        }
                    }
                }

                // Handler: set initial position
                readerViewHandlersActions.setInitialPosition = {
                    lifecycleScope.launch {
                        scrollToChapterItem(
                            lazyListState = lazyListState,
                            chapterIndex = it.chapterIndex,
                            chapterItemPosition = it.chapterItemPosition,
                            offset = it.chapterItemOffset,
                            items = items,
                        )
                    }
                }

                // Handler: maintain last visible position during content changes
                readerViewHandlersActions.maintainLastVisiblePosition = {
                    lifecycleScope.launch {
                        val oldSize = items.size
                        val firstVisibleIndex = lazyListState.firstVisibleItemIndex
                        val firstVisibleOffset = lazyListState.firstVisibleItemScrollOffset
                        val lastVisibleIndex = firstVisibleIndex +
                                lazyListState.layoutInfo.visibleItemsInfo.size - 1

                        it()

                        val displacement = items.size - oldSize
                        val newIndex = (lastVisibleIndex + displacement).coerceIn(
                            0,
                            items.size - 1,
                        )
                        lazyListState.scrollToItem(newIndex, firstVisibleOffset)
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            viewModel.onCloseManually()
            finish()
        }
    }

    private suspend fun scrollToChapterItem(
        lazyListState: androidx.compose.foundation.lazy.LazyListState,
        chapterIndex: Int,
        chapterItemPosition: Int,
        offset: Int,
        items: List<ReaderItem>,
    ) {
        val index = indexOfReaderItem(
            list = items,
            chapterIndex = chapterIndex,
            chapterItemPosition = chapterItemPosition,
        )
        if (index != -1) {
            lazyListState.scrollToItem(index, offset)
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
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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

    override fun onPause() {
        // Explicitly save to database when app pauses
        viewModel.saveCurrentReadingPosition()
        super.onPause()
    }
}
