package my.noveldoksuha.coreui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import my.noveldoksuha.coreui.R

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
    val context = LocalContext.current

    // Normalisasi model agar stabil
    val model = remember(imageModel, error) {
        when (imageModel) {
            is String -> imageModel.ifBlank { error }
            null -> error
            else -> imageModel
        }
    }

    if (LocalInspectionMode.current) {
        val res = if (model is Int) model else error
        Image(
            painter = painterResource(res),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            colorFilter = colorFilter,
        )
    } else {
        // Gunakan remember dengan key yang stabil
        val imageRequest = remember(model) {
            ImageRequest.Builder(context)
                .data(model)
                .crossfade(fadeInDurationMillis)
                .memoryCacheKey(model.toString()) // Penting: stabilkan cache key
                .diskCacheKey(model.toString()) // Optional: untuk disk cache
                .build()
        }

        val errorRequest = remember(error) {
            ImageRequest.Builder(context)
                .data(error)
                .crossfade(fadeInDurationMillis)
                .memoryCacheKey(error.toString())
                .diskCacheKey(error.toString())
                .build()
        }

        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            colorFilter = colorFilter,
            error = rememberAsyncImagePainter(
                model = errorRequest,
                contentScale = contentScale
            )
        )
    }
}
