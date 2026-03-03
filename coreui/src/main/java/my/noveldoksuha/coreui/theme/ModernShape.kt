package my.noveldoksuha.coreui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Modern Material 3 Shapes for NovelDokusha.
 * Larger corner radii for a cleaner, more contemporary look.
 */
val modernShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Modern image border shape with larger corner radius.
 */
val modernImageBorderShape = RoundedCornerShape(12.dp)

/**
 * Card shape for content containers.
 */
val cardShape = RoundedCornerShape(16.dp)

/**
 * Bottom sheet shape.
 */
val bottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

/**
 * Minimum height for selectable items (following Material 3 guidelines).
 * Modern theme uses larger touch target.
 */
val modernSelectableMinHeight = 56.dp

/**
 * Padding values for modern spacing.
 */
object ModernSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 24.dp
    val huge = 32.dp
}
