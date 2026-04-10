@file:Suppress("unused")

package my.noveldoksuha.coreui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Modern Vibrant Color Palette (Material You Inspired)
// ============================================================================

// Primary Accent - Modern Indigo (replaces old dull blue #2A59B6)
val ColorAccent = Color(0xFF6366F1) // Indigo 500 - vibrant & modern
val ColorAccentVariant = Color(0xFF818CF8) // Indigo 400 - lighter variant
val ColorAccentDark = Color(0xFF4F46E5) // Indigo 600 - darker variant

// Secondary Accent - Purple for variety
val ColorSecondary = Color(0xFF8B5CF6) // Violet 500
val ColorSecondaryVariant = Color(0xFFA78BFA) // Violet 400

// Tertiary Accent - Pink/Cyan for special highlights
val ColorTertiary = Color(0xFFEC4899) // Pink 500
val ColorTertiaryVariant = Color(0xFF06B6D4) // Cyan 500

// Functional Colors (replaces ColorLike and ColorNotice)
val ColorLike = Color(0xFFEF4444) // Red 500 - for favorites/bookmarks
val ColorLikeVariant = Color(0xFFF87171) // Red 400

val ColorNotice = Color(0xFFF59E0B) // Amber 500 - for warnings/notices
val ColorNoticeVariant = Color(0xFFFBBF24) // Amber 400

// Success & Info Colors (more vibrant)
val ColorSuccess = Color(0xFF10B981) // Emerald 500
val ColorSuccessVariant = Color(0xFF34D399) // Emerald 400

val ColorInfo = Color(0xFF3B82F6) // Blue 500
val ColorInfoVariant = Color(0xFF60A5FA) // Blue 400

// Error Color
val ColorError = Color(0xFFEF4444) // Red 500
val ColorErrorVariant = Color(0xFFF87171) // Red 400

// ============================================================================
// Neutral Grey Scale (Refined for better contrast)
// ============================================================================

val Grey0 = Color(0xFFFFFFFF)
val Grey25 = Color(0xFFFAFAFA)
val Grey50 = Color(0xFFF5F5F5)
val Grey75 = Color(0xFFEEEEEE)
val Grey100 = Color(0xFFE5E5E5)
val Grey200 = Color(0xFFD4D4D4)
val Grey300 = Color(0xFFA3A3A3)
val Grey400 = Color(0xFF737373)
val Grey500 = Color(0xFF525252)
val Grey600 = Color(0xFF404040)
val Grey700 = Color(0xFF262626)
val Grey800 = Color(0xFF171717)
val Grey900 = Color(0xFF0A0A0A)
val Grey1000 = Color(0xFF000000)

// ============================================================================
// Gradient Definitions (for modern UI elements)
// ============================================================================

val PrimaryGradient = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFF8B5CF6), // Violet
    Color(0xFFEC4899)  // Pink
)

val SecondaryGradient = listOf(
    Color(0xFF06B6D4), // Cyan
    Color(0xFF3B82F6), // Blue
    Color(0xFF6366F1)  // Indigo
)

val SuccessGradient = listOf(
    Color(0xFF10B981), // Emerald
    Color(0xFF14B8A6), // Teal
    Color(0xFF06B6D4)  // Cyan
)
