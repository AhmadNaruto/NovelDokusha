package my.noveldoksuha.coreui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import my.noveldoksuha.coreui.R

@Composable
fun ImageViewGlide(
    imageModel: Any?,
    modifier: Modifier = Modifier,
    fadeInDurationMillis: Int = 250,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes error: Int = R.drawable.default_book_cover
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
        Image(
            painter = painterResource(error),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        // Builder yang stabil, hanya dibuat sekali per model
        val requestBuilder = remember(model) {
            Glide
                .with(context)
                .asDrawable()
                .transition(DrawableTransitionOptions.withCrossFade(fadeInDurationMillis))
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache di disk
                .skipMemoryCache(false) // Aktifkan memory cache
        }

        GlideImage(
            imageModel = { model },
            requestBuilder = { requestBuilder },
            imageOptions = ImageOptions(
                contentDescription = contentDescription,
                contentScale = contentScale,
            ),
            modifier = modifier,
            failure = {
                GlideImage(
                    imageModel = { error },
                    requestBuilder = {
                        Glide.with(context)
                            .asDrawable()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                    }
                )
            }
        )
    }
}
