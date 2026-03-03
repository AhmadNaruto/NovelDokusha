package my.noveldoksuha.coreui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Modern skeleton loading components for better perceived performance.
 * Provides visual feedback while content is loading.
 */

/**
 * Shimmer effect for skeleton loaders.
 */
@Composable
private fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f
): Brush {
    return if (showShimmer) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val startOffsetX by transition.animateFloat(
            initialValue = -targetValue * 2,
            targetValue = targetValue * 2,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )
        Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.9f),
                Color.LightGray.copy(alpha = 0.3f),
                Color.LightGray.copy(alpha = 0.9f)
            ),
            start = Offset(startOffsetX, startOffsetX),
            end = Offset(startOffsetX + targetValue, startOffsetX + targetValue)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.LightGray, Color.LightGray)
        )
    }
}

/**
 * Basic skeleton box with shimmer effect.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = Dp.Unspecified,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    showShimmer: Boolean = true
) {
    Box(
        modifier = modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier)
            .then(if (height != Dp.Unspecified) Modifier.height(height) else Modifier)
            .clip(shape)
            .background(shimmerBrush(showShimmer))
    )
}

/**
 * Skeleton for book cover images.
 */
@Composable
fun SkeletonBookCover(
    modifier: Modifier = Modifier,
    showShimmer: Boolean = true
) {
    SkeletonBox(
        modifier = modifier
            .width(120.dp)
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        showShimmer = showShimmer
    )
}

/**
 * Skeleton for book item (cover + title + author).
 */
@Composable
fun SkeletonBookItem(
    modifier: Modifier = Modifier,
    showShimmer: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkeletonBookCover(showShimmer = showShimmer)
        SkeletonTextLine(
            width = 100.dp,
            height = 14.dp,
            showShimmer = showShimmer
        )
        SkeletonTextLine(
            width = 80.dp,
            height = 12.dp,
            showShimmer = showShimmer
        )
    }
}

/**
 * Skeleton for catalog grid items.
 */
@Composable
fun SkeletonCatalogGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    showShimmer: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(columns) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(2) {
                    SkeletonBookItem(showShimmer = showShimmer)
                }
            }
        }
    }
}

/**
 * Skeleton for text lines.
 */
@Composable
fun SkeletonTextLine(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = 16.dp,
    showShimmer: Boolean = true
) {
    SkeletonBox(
        modifier = modifier
            .fillMaxWidth()
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier)
            .height(height),
        shape = RoundedCornerShape(4.dp),
        showShimmer = showShimmer
    )
}

/**
 * Skeleton for settings items.
 */
@Composable
fun SkeletonSettingsItem(
    modifier: Modifier = Modifier,
    showShimmer: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonTextLine(
                width = 150.dp,
                height = 16.dp,
                showShimmer = showShimmer
            )
            SkeletonTextLine(
                width = 100.dp,
                height = 12.dp,
                showShimmer = showShimmer
            )
        }
        SkeletonBox(
            width = 48.dp,
            height = 48.dp,
            shape = RoundedCornerShape(24.dp),
            showShimmer = showShimmer
        )
    }
}

/**
 * Skeleton for chapter list items.
 */
@Composable
fun SkeletonChapterItem(
    modifier: Modifier = Modifier,
    showShimmer: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonTextLine(
                width = 200.dp,
                height = 16.dp,
                showShimmer = showShimmer
            )
            SkeletonTextLine(
                width = 120.dp,
                height = 12.dp,
                showShimmer = showShimmer
            )
        }
    }
}

/**
 * Full screen loading skeleton for reader.
 */
@Composable
fun SkeletonReaderContent(
    modifier: Modifier = Modifier,
    showShimmer: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        repeat(3) {
            SkeletonTextLine(
                height = 20.dp,
                showShimmer = showShimmer
            )
        }
        SkeletonBox(
            modifier = Modifier.fillMaxWidth(),
            height = 200.dp,
            shape = RoundedCornerShape(16.dp),
            showShimmer = showShimmer
        )
        repeat(5) {
            SkeletonTextLine(
                height = 16.dp,
                showShimmer = showShimmer
            )
        }
    }
}

/**
 * Placeholder image with skeleton effect.
 */
@Composable
fun SkeletonImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showShimmer: Boolean = true
) {
    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        SkeletonBox(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            showShimmer = showShimmer
        )
    }
}

/**
 * Spacer with skeleton effect.
 */
@Composable
fun SkeletonSpacer(
    height: Dp = 16.dp,
    showShimmer: Boolean = false
) {
    Spacer(modifier = Modifier.height(height))
}
