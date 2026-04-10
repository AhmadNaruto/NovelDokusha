package my.noveldoksuha.coreui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================================
// Modern Shape System - Following Material 3 guidelines
// More rounded for a friendly, modern look
// ============================================================================

val shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ============================================================================
// Pre-defined Shapes for Common Use Cases
// ============================================================================

// Card and container shapes
val cardShape = shapes.medium // 12dp
val elevatedCardShape = shapes.large // 16dp
val outlinedCardShape = shapes.medium // 12dp

// Image shapes
val ImageBorderShape = shapes.medium // 12dp for book covers
val avatarShape = shapes.extraLarge // 28dp (circular when width=height=28dp)
val thumbnailShape = shapes.small // 8dp

// Dialog and bottom sheet shapes
val dialogShape = shapes.extraLarge // 28dp
val bottomSheetShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 28.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

// Button shapes
val buttonShape = shapes.small // 8dp
val fabShape = shapes.large // 16dp

// Chip and badge shapes
val chipShape = shapes.small // 8dp
val badgeShape = RoundedCornerShape(12.dp) // Pill shape for badges

// Input field shapes
val inputFieldShape = shapes.medium // 12dp

// Minimum touch target size (Material Design guideline)
val selectableMinHeight = 48.dp
