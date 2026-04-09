package my.noveldoksuha.coreui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import my.noveldoksuha.coreui.R

/**
 * Alias for [ImageView] — Glide has been migrated to Coil.
 * All existing callers will now use Coil's disk cache automatically.
 */
@Composable
fun ImageViewGlide(
    imageModel: Any?,
    modifier: Modifier = Modifier,
    fadeInDurationMillis: Int = 250,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes error: Int = R.drawable.default_book_cover,
    size: Dp? = null,
) {
    ImageView(
        imageModel = imageModel,
        modifier = modifier,
        fadeInDurationMillis = fadeInDurationMillis,
        contentDescription = contentDescription,
        contentScale = contentScale,
        error = error,
        size = size,
    )
}