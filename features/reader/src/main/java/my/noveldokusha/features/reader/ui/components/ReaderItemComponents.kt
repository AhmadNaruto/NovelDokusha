package my.noveldokusha.features.reader.ui.components

import android.graphics.Typeface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import my.noveldokusha.core.AppFileResolver
import my.noveldokusha.features.reader.domain.ReaderItem

/**
 * Compose components for Reader feature.
 * Replaces the XML-based ReaderItemAdapter with Compose LazyColumn.
 */

// ============================================================================
// Main Reader Content
// ============================================================================

/**
 * Adds all reader items to a LazyColumn scope.
 */
internal fun LazyListScope.readerItems(
    items: List<ReaderItem>,
    bookUrl: String,
    textSelectability: Boolean,
    fontSize: Float,
    typeface: Typeface,
    typefaceBold: Typeface,
    onChapterStartVisible: (String) -> Unit,
    onChapterEndVisible: (String) -> Unit,
    onReloadReader: () -> Unit,
    onClick: () -> Unit,
    onImageClick: (ReaderItem.Image) -> Unit = {}
) {
    // Top padding
    item {
        ReaderPaddingItem(height = 700.dp)
    }

    // Reader items
    itemsIndexed(items) { index, item ->
        when (item) {
            is ReaderItem.Title -> ReaderTitleItem(
                text = item.textToDisplay,
                typeface = typefaceBold,
                fontSize = fontSize,
                textSelectability = textSelectability,
                onClick = onClick
            )
            is ReaderItem.Body -> ReaderBodyItem(
                text = item.textToDisplay,
                location = item.location,
                typeface = typeface,
                fontSize = fontSize,
                textSelectability = textSelectability,
                onChapterStartVisible = { onChapterStartVisible(item.chapterUrl) },
                onChapterEndVisible = { onChapterEndVisible(item.chapterUrl) },
                onClick = onClick
            )
            is ReaderItem.Image -> ReaderImageItem(
                image = item,
                bookUrl = bookUrl,
                location = item.location,
                onClick = { onImageClick(item) }
            )
            is ReaderItem.Error -> ReaderErrorItem(
                text = item.text,
                onReload = onReloadReader
            )
            is ReaderItem.Progressbar -> ReaderProgressItem()
            is ReaderItem.Divider -> ReaderDividerItem()
            is ReaderItem.Translating -> ReaderTranslatingItem(
                sourceLang = item.sourceLang,
                targetLang = item.targetLang
            )
            is ReaderItem.GoogleTranslateAttribution -> ReaderGoogleTranslateAttributionItem()
            is ReaderItem.TranslateAttribution -> ReaderTranslateAttributionItem(
                provider = item.provider
            )
            is ReaderItem.BookEnd -> ReaderBookEndItem(
                textSelectability = textSelectability,
                onClick = onClick
            )
            is ReaderItem.BookStart -> ReaderBookStartItem(
                textSelectability = textSelectability,
                onClick = onClick
            )
            is ReaderItem.Padding -> ReaderPaddingItem()
        }
    }

    // Bottom padding
    item {
        ReaderPaddingItem(height = 700.dp)
    }
}

// ============================================================================
// Individual Item Composables
// ============================================================================

@Composable
fun ReaderTitleItem(
    text: String,
    typeface: Typeface,
    fontSize: Float,
    textSelectability: Boolean,
    onClick: () -> Unit
) {
    val textModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp, horizontal = 24.dp)
        .pointerInput(onClick) {
            detectTapGestures(onTap = { onClick() })
        }

    if (textSelectability) {
        SelectionContainer {
            Text(
                text = text,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp,
                modifier = textModifier,
                textAlign = TextAlign.Center,
                lineHeight = (fontSize * 1.5f).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            modifier = textModifier,
            textAlign = TextAlign.Center,
            lineHeight = (fontSize * 1.5f).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ReaderBodyItem(
    text: String,
    location: ReaderItem.Location,
    typeface: Typeface,
    fontSize: Float,
    textSelectability: Boolean,
    onChapterStartVisible: () -> Unit,
    onChapterEndVisible: () -> Unit,
    onClick: () -> Unit
) {
    // Track visibility for chapter start/end detection
    androidx.compose.runtime.LaunchedEffect(location) {
        when (location) {
            ReaderItem.Location.FIRST -> onChapterStartVisible()
            ReaderItem.Location.LAST -> onChapterEndVisible()
            ReaderItem.Location.MIDDLE -> {}
        }
    }

    val textModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .pointerInput(onClick) {
            detectTapGestures(onTap = { onClick() })
        }

    if (textSelectability) {
        SelectionContainer {
            Text(
                text = text,
                fontFamily = FontFamily.Default,
                fontSize = fontSize.sp,
                modifier = textModifier,
                lineHeight = (fontSize * 1.5f).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontSize = fontSize.sp,
            modifier = textModifier,
            lineHeight = (fontSize * 1.5f).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ReaderImageItem(
    image: ReaderItem.Image,
    bookUrl: String,
    location: ReaderItem.Location,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appFileResolver = remember { AppFileResolver(context) }
    val imageUrl = remember(image, bookUrl) {
        appFileResolver.resolvedBookImagePath(bookUrl, image.image.path)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            error = ColorPainter(MaterialTheme.colorScheme.error)
        )
    }
}

@Composable
fun ReaderErrorItem(
    text: String,
    onReload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onReload) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reload")
        }
    }
}

@Composable
fun ReaderProgressItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ReaderDividerItem() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(vertical = 16.dp, horizontal = 16.dp),
    )
}

@Composable
fun ReaderTranslatingItem(
    sourceLang: String,
    targetLang: String
) {
    Text(
        text = "Translating from $sourceLang to $targetLang",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ReaderGoogleTranslateAttributionItem() {
    Text(
        text = "Translated by Google Translate",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ReaderTranslateAttributionItem(
    provider: String
) {
    val providerName = when (provider.lowercase()) {
        "gemini" -> "Google Gemini"
        "google" -> "Google Translate"
        else -> provider
    }
    Text(
        text = "Powered by $providerName",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ReaderBookEndItem(
    textSelectability: Boolean,
    onClick: () -> Unit
) {
    val textModifier = Modifier
        .fillMaxWidth()
        .padding(24.dp)
        .pointerInput(onClick) {
            detectTapGestures(onTap = { onClick() })
        }

    if (textSelectability) {
        SelectionContainer {
            Text(
                text = "No more chapters",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = textModifier,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Text(
            text = "No more chapters",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = textModifier,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReaderBookStartItem(
    textSelectability: Boolean,
    onClick: () -> Unit
) {
    val textModifier = Modifier
        .fillMaxWidth()
        .padding(24.dp)
        .pointerInput(onClick) {
            detectTapGestures(onTap = { onClick() })
        }

    if (textSelectability) {
        SelectionContainer {
            Text(
                text = "Beginning of book",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = textModifier,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Text(
            text = "Beginning of book",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = textModifier,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReaderPaddingItem(
    height: androidx.compose.ui.unit.Dp = 700.dp
) {
    Spacer(modifier = Modifier.height(height))
}
