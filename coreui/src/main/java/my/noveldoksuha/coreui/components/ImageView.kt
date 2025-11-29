package my.noveldoksuha.coreui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage                     // ✅ Coil 3
import coil3.request.ImageRequest                   // ✅ Coil 3
import coil3.request.crossfade                      // ✅ Coil 3
import my.noveldoksuha.coreui.R

/**
 * FIXED: request builder tanpa context.
 * AsyncImage otomatis mengambil context dari CompositionLocal.
 */
@Composable
fun ImageView(
    imageModel: Any?,
    modifier: Modifier = Modifier,
    fadeInDurationMillis: Int = 250,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes error: Int = R.drawable.default_book_cover,
    colorFilter: ColorFilter? = null,
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(error),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            colorFilter = colorFilter
        )
        return
    }

    val context = LocalContext.current
    val data = (imageModel as? String)?.takeIf { it.isNotBlank() } ?: imageModel

    AsyncImage(
        model = ImageRequest.Builder() // ✅ HAPUS "context"
            .data(data ?: error) // null/blank langsung pakai drawable
            .crossfade(durationMillis = fadeInDurationMillis)
            .placeholder(error)
            .error(error)
            .memoryCachePolicy(
                if (fadeInDurationMillis > 0) coil3.request.CachePolicy.ENABLED
                else coil3.request.CachePolicy.READ_ONLY
            )
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        colorFilter = colorFilter
    )
}
