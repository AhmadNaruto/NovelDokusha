package my.noveldoksuha.coreui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import my.noveldoksuha.coreui.R

/**
 * Coil-based image component with disk cache support.
 *
 * @param imageModel URL, file path, or drawable resource
 * @param modifier Layout modifier
 * @param fadeInDurationMillis Crossfade animation duration (0 to disable)
 * @param contentDescription Accessibility description
 * @param contentScale How the image should be scaled
 * @param error Fallback drawable resource
 * @param colorFilter Optional color filter
 * @param size Override decoded image size in pixels. When set, Coil decodes
 *   a smaller bitmap which reduces memory usage — critical for grid lists
 *   with many cover images.
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
    size: Dp? = null,
) {
    val model by remember(imageModel, error) {
        derivedStateOf {
            when (imageModel) {
                is String -> imageModel.ifBlank { error }
                null -> run { error }
                else -> imageModel
            }
        }
    }
    if (LocalInspectionMode.current) {
        val res = when (val modelCopy = model) {
            is Int -> modelCopy
            else -> error
        }
        Image(
            painter = painterResource(res),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            colorFilter = colorFilter,
        )
    } else {
        val context by rememberUpdatedState(LocalContext.current)
        val density = LocalDensity.current

        val imageRequest by remember(model, size) {
            derivedStateOf {
                val pixelSize = size?.let { with(density) { it.toPx() }.toInt() }
                ImageRequest
                    .Builder(context)
                    .data(model)
                    .crossfade(fadeInDurationMillis)
                    .apply {
                        // Disk cache for cover images — survives app restarts
                        diskCachePolicy(CachePolicy.ENABLED)
                        memoryCachePolicy(CachePolicy.ENABLED)
                    }
                    .apply {
                        if (pixelSize != null) {
                            size(pixelSize, pixelSize)
                        }
                    }
                    .build()
            }
        }
        val imageErrorRequest by remember(error) {
            derivedStateOf {
                ImageRequest
                    .Builder(context)
                    .data(error)
                    .crossfade(fadeInDurationMillis)
                    .build()
            }
        }
        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            colorFilter = colorFilter,
            error = rememberAsyncImagePainter(
                model = imageErrorRequest,
                contentScale = contentScale
            )
        )
    }
}