package my.noveldoksuha.coreui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.AppTestTags
import my.noveldoksuha.coreui.R
import my.noveldoksuha.coreui.theme.Grey25
import my.noveldoksuha.coreui.theme.Grey800
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
    indication: Indication = LocalIndication.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit,
    onLongClick: () -> Unit = { },
) {
    Column(modifier = modifier.testTag(AppTestTags.BOOK_IMAGE_BUTTON_VIEW)) {
        Box(
            Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .aspectRatio(1 / 1.45f)
                .clip(ImageBorderShape)
                .background(MaterialTheme.colorApp.bookSurface)
                .combinedClickable(
                    indication = indication,
                    interactionSource = interactionSource,
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            ImageView(
                imageModel = coverImageModel,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                error = R.drawable.default_book_cover,
            )
            if (bookTitlePosition == BookTitlePosition.Inside) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                0f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.0f),
                                0.4f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                1f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            )
                        )
                        .padding(top = 30.dp, bottom = 8.dp)
                        .padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Grey800,
                        drawStyle = Stroke(
                            miter = 4f,
                            width = 4f,
                            join = StrokeJoin.Miter
                        )
                    )
                )
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 30.dp, bottom = 8.dp)
                        .padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Grey25,
                    )
                )
            }
        }
        if (bookTitlePosition == BookTitlePosition.Outside) {
            Text(
                text = title,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(4.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
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