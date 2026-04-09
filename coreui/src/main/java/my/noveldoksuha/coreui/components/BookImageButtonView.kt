package my.noveldoksuha.coreui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.noveldoksuha.coreui.AppTestTags
import my.noveldoksuha.coreui.R
import my.noveldoksuha.coreui.theme.Grey25
import my.noveldoksuha.coreui.theme.ImageBorderShape
import my.noveldoksuha.coreui.theme.InternalTheme
import my.noveldoksuha.coreui.theme.PreviewThemes
import my.noveldoksuha.coreui.theme.colorApp

enum class BookTitlePosition {
    Inside, Outside, Hidden
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookImageButtonView(
    title: String,
    coverImageModel: Any,
    modifier: Modifier = Modifier,
    bookTitlePosition: BookTitlePosition = BookTitlePosition.Inside,
    indication: androidx.compose.foundation.Indication = LocalIndication.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit,
    onLongClick: () -> Unit = { },
) {
    val surfaceColor = MaterialTheme.colorApp.bookSurface

    Column(modifier = modifier.testTag(AppTestTags.BOOK_IMAGE_BUTTON_VIEW)) {
        Card(
            shape = ImageBorderShape,
            colors = CardDefaults.cardColors(
                containerColor = surfaceColor,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp,
            ),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .aspectRatio(1 / 1.45f)
                .combinedClickable(
                    indication = indication,
                    interactionSource = interactionSource,
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
        ) {
            Box {
                // Shimmer-like gradient background behind cover
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    surfaceColor,
                                    surfaceColor.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                            )
                        )
                )

                ImageView(
                    imageModel = coverImageModel,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    error = R.drawable.default_book_cover,
                )

                if (bookTitlePosition == BookTitlePosition.Inside) {
                    // Modern title overlay with clean gradient backdrop
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        // Gradient backdrop
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        0.3f to Color.Black.copy(alpha = 0.15f),
                                        0.7f to Color.Black.copy(alpha = 0.55f),
                                        1f to Color.Black.copy(alpha = 0.75f),
                                    )
                                )
                                .padding(top = 40.dp)
                        )

                        // Title text with shadow instead of stroke outline
                        Text(
                            text = title,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Grey25,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.8f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 4f,
                                ),
                                lineHeight = 14.sp,
                                fontSize = 12.sp,
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        if (bookTitlePosition == BookTitlePosition.Outside) {
            Text(
                text = title,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp,
                ),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewThemes
@Composable
private fun PreviewView() {
    InternalTheme {
        Row {
            BookImageButtonView(
                title = "Hello there",
                coverImageModel = "",
                onClick = { },
                onLongClick = { },
                bookTitlePosition = BookTitlePosition.Inside,
                modifier = Modifier.weight(1f)
            )
            BookImageButtonView(
                title = "Hello there text very long for a title, but many cases just like this",
                coverImageModel = "",
                onClick = { },
                onLongClick = { },
                bookTitlePosition = BookTitlePosition.Outside,
                modifier = Modifier.weight(1f)
            )
        }
    }
}