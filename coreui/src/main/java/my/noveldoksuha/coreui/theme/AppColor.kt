package my.noveldoksuha.coreui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColor(
    val tabSurface: Color,
    val bookSurface: Color,
    val checkboxPositive: Color,
    val checkboxNegative: Color,
    val checkboxNeutral: Color,
    val tintedSurface: Color,
    val tintedSelectedSurface: Color,
    val badgeBackground: Color,
    val gradientPrimary: List<Color>,
    val gradientSecondary: List<Color>,
)

val light_appColor = AppColor(
    tabSurface = Grey75,
    bookSurface = Grey50,
    checkboxPositive = ColorSuccess,
    checkboxNegative = ColorError,
    checkboxNeutral = Grey400,
    tintedSurface = ColorAccent.copy(alpha = 0.08f),
    tintedSelectedSurface = ColorAccent.copy(alpha = 0.12f),
    badgeBackground = ColorAccent,
    gradientPrimary = PrimaryGradient,
    gradientSecondary = SecondaryGradient,
)

val dark_appColor = AppColor(
    tabSurface = Grey800,
    bookSurface = Grey800,
    checkboxPositive = ColorSuccessVariant,
    checkboxNegative = ColorErrorVariant,
    checkboxNeutral = Grey600,
    tintedSurface = ColorAccent.copy(alpha = 0.15f),
    tintedSelectedSurface = ColorAccent.copy(alpha = 0.2f),
    badgeBackground = ColorAccentVariant,
    gradientPrimary = PrimaryGradient,
    gradientSecondary = SecondaryGradient,
)

val black_appColor = AppColor(
    tabSurface = Grey900,
    bookSurface = Grey900,
    checkboxPositive = ColorSuccessVariant,
    checkboxNegative = ColorErrorVariant,
    checkboxNeutral = Grey600,
    tintedSurface = ColorAccent.copy(alpha = 0.18f),
    tintedSelectedSurface = ColorAccent.copy(alpha = 0.25f),
    badgeBackground = ColorAccentVariant,
    gradientPrimary = PrimaryGradient,
    gradientSecondary = SecondaryGradient,
)

val LocalAppColor = compositionLocalOf { light_appColor }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.colorApp: AppColor
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColor.current