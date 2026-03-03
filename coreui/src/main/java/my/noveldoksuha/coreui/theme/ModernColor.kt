package my.noveldoksuha.coreui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Modern Material 3 Color Scheme for NovelDokusha.
 * Enhanced with better contrast and contemporary colors.
 */

// Primary brand color - Modern Blue
val ModernPrimary = Color(0xFF2563EB)
val ModernOnPrimary = Color(0xFFFFFFFF)
val ModernPrimaryContainer = Color(0xFFDBE7FE)
val ModernOnPrimaryContainer = Color(0xFF1D4ED8)

// Secondary color - Complementary Purple
val ModernSecondary = Color(0xFF7C3AED)
val ModernOnSecondary = Color(0xFFFFFFFF)
val ModernSecondaryContainer = Color(0xFFEDE9FE)
val ModernOnSecondaryContainer = Color(0xFF6D28D9)

// Tertiary color - Accent Teal
val ModernTertiary = Color(0xFF14B8A6)
val ModernOnTertiary = Color(0xFFFFFFFF)
val ModernTertiaryContainer = Color(0xFFCCFBF1)
val ModernOnTertiaryContainer = Color(0xFF0F766E)

// Error colors
val ModernError = Color(0xFFDC2626)
val ModernOnError = Color(0xFFFFFFFF)
val ModernErrorContainer = Color(0xFFFEE2E2)
val ModernOnErrorContainer = Color(0xFFB91C1C)

// Light theme colors
val ModernLightBackground = Color(0xFFFAFAFA)
val ModernLightOnBackground = Color(0xFF171717)
val ModernLightSurface = Color(0xFFFFFFFF)
val ModernLightOnSurface = Color(0xFF171717)
val ModernLightSurfaceVariant = Color(0xFFE5E7EB)
val ModernLightOnSurfaceVariant = Color(0xFF44403C)
val ModernLightOutline = Color(0xFFA8A29E)

// Dark theme colors
val ModernDarkBackground = Color(0xFF0A0A0A)
val ModernDarkOnBackground = Color(0xFFEDEDED)
val ModernDarkSurface = Color(0xFF171717)
val ModernDarkOnSurface = Color(0xFFEDEDED)
val ModernDarkSurfaceVariant = Color(0xFF262626)
val ModernDarkOnSurfaceVariant = Color(0xFFE5E5E5)
val ModernDarkOutline = Color(0xFF525252)

// Black theme (AMOLED)
val BlackBackground = Color(0xFF000000)
val BlackSurface = Color(0xFF000000)
val BlackSurfaceVariant = Color(0xFF121212)

/**
 * Modern light color scheme with improved contrast.
 */
val modernLightColorScheme = lightColorScheme(
    primary = ModernPrimary,
    onPrimary = ModernOnPrimary,
    primaryContainer = ModernPrimaryContainer,
    onPrimaryContainer = ModernOnPrimaryContainer,
    secondary = ModernSecondary,
    onSecondary = ModernOnSecondary,
    secondaryContainer = ModernSecondaryContainer,
    onSecondaryContainer = ModernOnSecondaryContainer,
    tertiary = ModernTertiary,
    onTertiary = ModernOnTertiary,
    tertiaryContainer = ModernTertiaryContainer,
    onTertiaryContainer = ModernOnTertiaryContainer,
    error = ModernError,
    onError = ModernOnError,
    errorContainer = ModernErrorContainer,
    onErrorContainer = ModernOnErrorContainer,
    background = ModernLightBackground,
    onBackground = ModernLightOnBackground,
    surface = ModernLightSurface,
    onSurface = ModernLightOnSurface,
    surfaceVariant = ModernLightSurfaceVariant,
    onSurfaceVariant = ModernLightOnSurfaceVariant,
    outline = ModernLightOutline
)

/**
 * Modern dark color scheme with better readability.
 */
val modernDarkColorScheme = darkColorScheme(
    primary = ModernPrimary,
    onPrimary = ModernOnPrimary,
    primaryContainer = Color(0xFF1E40AF),
    onPrimaryContainer = Color(0xFFDBE7FE),
    secondary = ModernSecondary,
    onSecondary = ModernOnSecondary,
    secondaryContainer = Color(0xFF5B21B6),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = ModernTertiary,
    onTertiary = ModernOnTertiary,
    tertiaryContainer = Color(0xFF0F766E),
    onTertiaryContainer = Color(0xFFCCFBF1),
    error = Color(0xFFF87171),
    onError = ModernError,
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),
    background = ModernDarkBackground,
    onBackground = ModernDarkOnBackground,
    surface = ModernDarkSurface,
    onSurface = ModernDarkOnSurface,
    surfaceVariant = ModernDarkSurfaceVariant,
    onSurfaceVariant = ModernDarkOnSurfaceVariant,
    outline = ModernDarkOutline
)

/**
 * Black color scheme for AMOLED displays.
 */
val modernBlackColorScheme = darkColorScheme(
    primary = ModernPrimary,
    onPrimary = ModernOnPrimary,
    primaryContainer = Color(0xFF1E40AF),
    onPrimaryContainer = Color(0xFFDBE7FE),
    secondary = ModernSecondary,
    onSecondary = ModernOnSecondary,
    secondaryContainer = Color(0xFF5B21B6),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = ModernTertiary,
    onTertiary = ModernOnTertiary,
    tertiaryContainer = Color(0xFF0F766E),
    onTertiaryContainer = Color(0xFFCCFBF1),
    error = Color(0xFFF87171),
    onError = ModernError,
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),
    background = BlackBackground,
    onBackground = ModernDarkOnBackground,
    surface = BlackSurface,
    onSurface = ModernDarkOnSurface,
    surfaceVariant = BlackSurfaceVariant,
    onSurfaceVariant = ModernDarkOnSurfaceVariant,
    outline = ModernDarkOutline
)
