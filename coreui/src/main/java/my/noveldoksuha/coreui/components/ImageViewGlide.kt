package my.noveldoksuha.coreui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import my.noveldoksuha.coreui.R

/**
 * @deprecated Use [ImageView] instead. This function is kept for backward compatibility.
 */
@Deprecated("Use ImageView instead", ReplaceWith("ImageView(imageModel, modifier, fadeInDurationMillis, contentDescription, contentScale, error)"))
@Composable
fun ImageViewGlide(
    imageModel: Any?,
    modifier: Modifier = Modifier,
    fadeInDurationMillis: Int = 250,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes error: Int = R.drawable.default_book_cover
) {
    ImageView(
        imageModel = imageModel,
        modifier = modifier,
        fadeInDurationMillis = fadeInDurationMillis,
        contentDescription = contentDescription,
        contentScale = contentScale,
        error = error
    )
}
