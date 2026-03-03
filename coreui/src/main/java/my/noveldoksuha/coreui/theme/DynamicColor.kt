package my.noveldoksuha.coreui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Dynamic Color (Material You) support for Android 12+.
 * Extracts colors from user's wallpaper for personalized theme.
 */

/**
 * Check if device supports dynamic colors (Android 12S+)
 */
val isDynamicColorSupported: Boolean
    @Composable
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Get dynamic color scheme based on system theme.
 * Falls back to modern color scheme if not supported.
 */
@Composable
fun getDynamicColorScheme(
    useDynamicColor: Boolean = true
): androidx.compose.material3.ColorScheme {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    
    return if (useDynamicColor && isDynamicColorSupported) {
        if (isDarkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        if (isDarkTheme) {
            modernDarkColorScheme
        } else {
            modernLightColorScheme
        }
    }
}

/**
 * Theme configuration with dynamic color support.
 */
data class ModernThemeConfig(
    val useModernTheme: Boolean = true,
    val useDynamicColor: Boolean = true,
    val theme: Themes = Themes.LIGHT
)
