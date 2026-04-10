package my.noveldoksuha.coreui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun ColorScheme.isLightTheme() = background.luminance() > 0.5

// ============================================================================
// Light Color Scheme - Modern M3 with proper tonal relationships
// ============================================================================

val light_colorScheme = lightColorScheme(
    primary = ColorAccent,
    onPrimary = Color.White,
    primaryContainer = ColorAccentVariant.copy(alpha = 0.25f),
    onPrimaryContainer = ColorAccentDark,
    
    secondary = ColorSecondary,
    onSecondary = Color.White,
    secondaryContainer = ColorSecondaryVariant.copy(alpha = 0.2f),
    onSecondaryContainer = ColorSecondary,
    
    tertiary = ColorTertiary,
    onTertiary = Color.White,
    tertiaryContainer = ColorTertiary.copy(alpha = 0.15f),
    onTertiaryContainer = ColorTertiaryVariant,
    
    background = Grey25,
    onBackground = Grey900,
    
    surface = Grey25,
    onSurface = Grey900,
    surfaceVariant = Grey75,
    onSurfaceVariant = Grey800,
    surfaceTint = ColorAccent,
    surfaceContainerLowest = Grey0,
    surfaceContainerLow = Grey50,
    surfaceContainer = Grey75,
    surfaceContainerHigh = Grey100,
    surfaceContainerHighest = Grey200,
    
    inverseSurface = Grey900,
    inverseOnSurface = Grey25,
    inversePrimary = ColorAccentVariant,
    
    error = ColorError,
    onError = Color.White,
    errorContainer = ColorError.copy(alpha = 0.15f),
    onErrorContainer = ColorErrorVariant,
    
    outline = Grey400,
    outlineVariant = Grey200,
    scrim = Color.Black.copy(alpha = 0.4f),
)

// ============================================================================
// Dark Color Scheme - AMOLED-friendly with vibrant accents
// ============================================================================

val dark_colorScheme = darkColorScheme(
    primary = ColorAccentVariant,
    onPrimary = ColorAccentDark,
    primaryContainer = ColorAccent.copy(alpha = 0.3f),
    onPrimaryContainer = ColorAccentVariant,
    
    secondary = ColorSecondaryVariant,
    onSecondary = ColorSecondary,
    secondaryContainer = ColorSecondary.copy(alpha = 0.25f),
    onSecondaryContainer = ColorSecondaryVariant,
    
    tertiary = ColorTertiaryVariant,
    onTertiary = ColorTertiary,
    tertiaryContainer = ColorTertiary.copy(alpha = 0.2f),
    onTertiaryContainer = ColorTertiaryVariant,
    
    background = Grey900,
    onBackground = Grey50,
    
    surface = Grey900,
    onSurface = Grey50,
    surfaceVariant = Grey800,
    onSurfaceVariant = Grey200,
    surfaceTint = ColorAccentVariant,
    surfaceContainerLowest = Grey1000,
    surfaceContainerLow = Grey900,
    surfaceContainer = Grey800,
    surfaceContainerHigh = Grey700,
    surfaceContainerHighest = Grey600,
    
    inverseSurface = Grey25,
    inverseOnSurface = Grey900,
    inversePrimary = ColorAccent,
    
    error = ColorErrorVariant,
    onError = ColorError,
    errorContainer = ColorError.copy(alpha = 0.2f),
    onErrorContainer = ColorErrorVariant,
    
    outline = Grey400,
    outlineVariant = Grey700,
    scrim = Color.Black.copy(alpha = 0.6f),
)

// ============================================================================
// Black Color Scheme - True black for AMOLED with vibrant accents
// ============================================================================

val black_colorScheme = darkColorScheme(
    primary = ColorAccentVariant,
    onPrimary = ColorAccentDark,
    primaryContainer = ColorAccent.copy(alpha = 0.35f),
    onPrimaryContainer = ColorAccentVariant,
    
    secondary = ColorSecondaryVariant,
    onSecondary = ColorSecondary,
    secondaryContainer = ColorSecondary.copy(alpha = 0.3f),
    onSecondaryContainer = ColorSecondaryVariant,
    
    tertiary = ColorTertiaryVariant,
    onTertiary = ColorTertiary,
    tertiaryContainer = ColorTertiary.copy(alpha = 0.25f),
    onTertiaryContainer = ColorTertiaryVariant,
    
    background = Grey1000, // True black
    onBackground = Grey50,
    
    surface = Grey1000, // True black
    onSurface = Grey50,
    surfaceVariant = Grey900,
    onSurfaceVariant = Grey200,
    surfaceTint = ColorAccentVariant,
    surfaceContainerLowest = Grey1000,
    surfaceContainerLow = Grey900,
    surfaceContainer = Grey900,
    surfaceContainerHigh = Grey800,
    surfaceContainerHighest = Grey700,
    
    inverseSurface = Grey25,
    inverseOnSurface = Grey1000,
    inversePrimary = ColorAccent,
    
    error = ColorErrorVariant,
    onError = ColorError,
    errorContainer = ColorError.copy(alpha = 0.25f),
    onErrorContainer = ColorErrorVariant,
    
    outline = Grey400,
    outlineVariant = Grey800,
    scrim = Color.Black.copy(alpha = 0.7f),
)
