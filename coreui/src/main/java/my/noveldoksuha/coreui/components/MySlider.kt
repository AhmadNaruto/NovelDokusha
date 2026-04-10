package my.noveldoksuha.coreui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.theme.InternalTheme

// ============================================================================
// Modern Slider Component - Material 3 Compliant
// Replaces custom draggable implementation with M3 Slider
// ============================================================================

/**
 * Modern Slider with label and value display
 * 
 * Usage:
 * ```
 * ModernSlider(
 *     value = sliderValue,
 *     onValueChange = { sliderValue = it },
 *     valueRange = 0f..100f,
 *     label = "Volume",
 *     showValue = true
 * )
 * ```
 */
@Composable
fun ModernSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    label: String? = null,
    showValue: Boolean = true,
    steps: Int = 0,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
    ),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label and value display
        if (label != null || showValue) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (showValue) {
                    Text(
                        text = formatSliderValue(value),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // M3 Slider
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = colors,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Modern Slider with custom overlay content (backward compatible with old MySlider API)
 */
@Composable
fun ModernSliderWithOverlay(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    overlayContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )
        overlayContent()
    }
}

/**
 * Helper function to format slider value
 */
private fun formatSliderValue(value: Float): String {
    return if (value == value.toLong().toFloat()) {
        value.toLong().toString()
    } else {
        String.format("%.1f", value)
    }
}

// ============================================================================
// Backward Compatibility - Keep old MySlider API but use M3 Slider internally
// ============================================================================

@Composable
fun MySlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    ModernSlider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        label = text,
        showValue = true,
        modifier = modifier
    )
}

@Composable
fun MySlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    overlayContent: @Composable BoxScope.() -> Unit,
) {
    ModernSliderWithOverlay(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        overlayContent = overlayContent,
        modifier = modifier
    )
}

// ============================================================================
// Preview
// ============================================================================

@Preview(heightDp = 120, widthDp = 500)
@Composable
fun ModernSliderPreview() {
    var value by remember { mutableFloatStateOf(50f) }
    InternalTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ModernSlider(
                value = value,
                onValueChange = { value = it },
                valueRange = 0f..100f,
                label = "Volume",
                showValue = true
            )
            
            ModernSlider(
                value = value / 100f,
                onValueChange = { value = it * 100f },
                valueRange = 0f..1f,
                label = "Brightness",
                showValue = true,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
