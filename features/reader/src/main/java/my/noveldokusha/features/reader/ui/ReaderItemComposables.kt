package my.noveldokusha.features.reader.ui

import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import my.noveldoksuha.coreui.components.ImageView
import my.noveldokusha.core.AppFileResolver
import my.noveldokusha.features.reader.domain.ReaderItem
import my.noveldokusha.reader.R

// ---------------------------------------------------------------------------
// Helper: reader text colour driven by Material colour scheme
// ---------------------------------------------------------------------------
@Composable
private fun readerTextColor(): Color = MaterialTheme.colorScheme.onSurface

// ---------------------------------------------------------------------------
// 1. Body paragraph
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderBodyItem(
    item: ReaderItem.Body,
    fontSize: Float,
    typeface: Typeface,
    selectable: Boolean,
    onClick: () -> Unit,
    onChapterStartVisible: (chapterUrl: String) -> Unit,
    onChapterEndVisible: (chapterUrl: String) -> Unit,
) {
    val paragraph = item.textToDisplay + "\n"
    val textColor = readerTextColor()

    when (item.location) {
        ReaderItem.Location.FIRST -> onChapterStartVisible(item.chapterUrl)
        ReaderItem.Location.LAST -> onChapterEndVisible(item.chapterUrl)
        else -> {}
    }

    val textComposable: @Composable () -> Unit = {
        Text(
            text = paragraph,
            fontSize = fontSize.sp,
            fontWeight = if ((typeface.style and android.graphics.Typeface.BOLD) != 0) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onClick),
        )
    }

    if (selectable) SelectionContainer { textComposable() } else textComposable()
}

// ---------------------------------------------------------------------------
// 2. Chapter title with decoration icon
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderTitleItem(
    item: ReaderItem.Title,
    typefaceBold: Typeface,
    selectable: Boolean,
    onClick: () -> Unit,
) {
    val textColor = readerTextColor()
    val decorationPainter = painterResource(id = R.drawable.ic_decoration_round)

    // typefaceBold is available for future font mapping; currently using FontWeight.Bold
    @Suppress("UNUSED_VARIABLE") val _typefaceBold = typefaceBold

    val textComposable: @Composable () -> Unit = {
        Text(
            text = item.textToDisplay,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 17.dp, top = 46.dp, end = 17.dp, bottom = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = decorationPainter,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 12.dp),
            colorFilter = ColorFilter.tint(Color(0xFFBBBBBB)),
        )
        if (selectable) SelectionContainer { textComposable() } else textComposable()
    }
}

// ---------------------------------------------------------------------------
// 3. Image with dynamic aspect ratio
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderImageItem(
    item: ReaderItem.Image,
    bookUrl: String,
    onClick: () -> Unit,
    onChapterStartVisible: (chapterUrl: String) -> Unit,
    onChapterEndVisible: (chapterUrl: String) -> Unit,
) {
    val context = LocalContext.current
    val imageModel = remember(context, bookUrl, item.image.path) {
        AppFileResolver(context).resolvedBookImagePath(
            bookUrl = bookUrl,
            imagePath = item.image.path,
        )
    }

    when (item.location) {
        ReaderItem.Location.FIRST -> onChapterStartVisible(item.chapterUrl)
        ReaderItem.Location.LAST -> onChapterEndVisible(item.chapterUrl)
        else -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 10.dp, bottom = 25.dp),
    ) {
        ImageView(
            imageModel = imageModel,
            contentDescription = stringResource(id = R.string.image),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(),
            error = R.drawable.ic_baseline_error_outline_24,
        )
    }
}

// ---------------------------------------------------------------------------
// 4. Special title (BookEnd / BookStart)
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderSpecialTitleItem(
    text: String,
    selectable: Boolean,
    onClick: () -> Unit,
) {
    val textColor = readerTextColor()

    val textComposable: @Composable () -> Unit = {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(17.dp),
        )
    }

    if (selectable) SelectionContainer { textComposable() } else textComposable()
}

// ---------------------------------------------------------------------------
// 5. Error with reload button
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderErrorItem(
    item: ReaderItem.Error,
    selectable: Boolean,
    onReload: () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(17.dp)
            .clickable(onClick = onClick),
    ) {
        Button(
            onClick = onReload,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = stringResource(id = R.string.reload))
        }
        val errorTextComposable: @Composable () -> Unit = {
            Text(
                text = item.text,
                color = Color(0xFFF44336),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (selectable) SelectionContainer { errorTextComposable() } else errorTextComposable()
    }
}

// ---------------------------------------------------------------------------
// 6. Progress bar
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderProgressBarItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

// ---------------------------------------------------------------------------
// 7. Translating status text
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderTranslatingItem(item: ReaderItem.Translating) {
    val textColor = readerTextColor()
    Text(
        text = stringResource(
            id = R.string.translating_from_lang_a_to_lang_b,
            item.sourceLang,
            item.targetLang,
        ),
        color = textColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
        textAlign = TextAlign.Center,
    )
}

// ---------------------------------------------------------------------------
// 8. Translation attribution
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderTranslateAttributionItem(item: ReaderItem.TranslateAttribution) {
    val text = when (item.provider) {
        "gemini" -> "Powered by Gemini"
        else -> "Powered by Google Translate"
    }
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    Text(
        text = text,
        fontSize = 11.sp,
        fontStyle = FontStyle.Italic,
        color = textColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        textAlign = TextAlign.End,
    )
}

// ---------------------------------------------------------------------------
// 9. Google Translate attribution logo
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderGoogleTranslateAttributionItem() {
    val contentColor = MaterialTheme.colorScheme.onSurface
    val drawable = androidx.core.content.res.ResourcesCompat.getDrawable(
        LocalContext.current.resources,
        R.drawable.google_translate_attribution_greyscale,
        null,
    )
    val imageBitmap = drawable?.toBitmap()?.asImageBitmap()

    if (imageBitmap != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                colorFilter = ColorFilter.tint(contentColor),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 10. Horizontal divider
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderDividerItem() {
    HorizontalDivider(thickness = 1.dp)
}

// ---------------------------------------------------------------------------
// 11. Top/bottom padding spacer
// ---------------------------------------------------------------------------
@Composable
internal fun ReaderPaddingItem() {
    val density = LocalDensity.current
    val heightDp = with(density) { 700.sp.toDp() }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp),
    )
}

// ============================================================================
// LazyColumn wrapper
// ============================================================================

@Composable
internal fun ReaderLazyList(
    items: List<ReaderItem>,
    bookUrl: String,
    fontSize: Float,
    typeface: Typeface,
    typefaceBold: Typeface,
    selectable: Boolean,
    lazyListState: LazyListState = rememberLazyListState(),
    onChapterStartVisible: (chapterUrl: String) -> Unit,
    onChapterEndVisible: (chapterUrl: String) -> Unit,
    onReloadReader: () -> Unit,
    onClick: () -> Unit,
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Top padding
        item { ReaderPaddingItem() }

        items(
            items = items,
            key = { readerItem ->
                when (readerItem) {
                    is ReaderItem.Position -> "${readerItem.chapterIndex}_${readerItem.chapterItemPosition}_${readerItem::class.simpleName}"
                    else -> "${readerItem.chapterIndex}_${readerItem::class.simpleName}_${System.identityHashCode(readerItem)}"
                }
            },
        ) { readerItem ->
            when (readerItem) {
                is ReaderItem.Body -> ReaderBodyItem(
                    item = readerItem,
                    fontSize = fontSize,
                    typeface = typeface,
                    selectable = selectable,
                    onClick = onClick,
                    onChapterStartVisible = onChapterStartVisible,
                    onChapterEndVisible = onChapterEndVisible,
                )
                is ReaderItem.Title -> ReaderTitleItem(
                    item = readerItem,
                    typefaceBold = typefaceBold,
                    selectable = selectable,
                    onClick = onClick,
                )
                is ReaderItem.Image -> ReaderImageItem(
                    item = readerItem,
                    bookUrl = bookUrl,
                    onClick = onClick,
                    onChapterStartVisible = onChapterStartVisible,
                    onChapterEndVisible = onChapterEndVisible,
                )
                is ReaderItem.BookEnd -> ReaderSpecialTitleItem(
                    text = stringResource(id = R.string.reader_no_more_chapters),
                    selectable = selectable,
                    onClick = onClick,
                )
                is ReaderItem.BookStart -> ReaderSpecialTitleItem(
                    text = stringResource(id = R.string.reader_first_chapter),
                    selectable = selectable,
                    onClick = onClick,
                )
                is ReaderItem.Error -> ReaderErrorItem(
                    item = readerItem,
                    selectable = selectable,
                    onReload = onReloadReader,
                    onClick = onClick,
                )
                is ReaderItem.Progressbar -> ReaderProgressBarItem()
                is ReaderItem.Translating -> ReaderTranslatingItem(item = readerItem)
                is ReaderItem.TranslateAttribution -> ReaderTranslateAttributionItem(item = readerItem)
                is ReaderItem.GoogleTranslateAttribution -> ReaderGoogleTranslateAttributionItem()
                is ReaderItem.Divider -> ReaderDividerItem()
                is ReaderItem.Padding -> ReaderPaddingItem()
            }
        }

        // Bottom padding
        item { ReaderPaddingItem() }
    }
}
