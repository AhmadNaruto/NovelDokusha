package my.noveldoksuha.coreui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import my.noveldoksuha.coreui.theme.colorApp
import my.noveldoksuha.coreui.theme.dialogShape

// ============================================================================
// Modern Dialog Components - Material 3 Compliant
// ============================================================================

/**
 * Modern Dialog with icon, title, content, and actions
 * 
 * Usage:
 * ```
 * ModernDialog(
 *     onDismiss = { showDialog = false },
 *     icon = Icons.Default.Info,
 *     title = "Dialog Title",
 *     confirmText = "OK",
 *     onConfirm = { /* action */ }
 * ) {
 *     Text("Dialog content here")
 * }
 * ```
 */
@Composable
fun ModernDialog(
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    title: String,
    confirmText: String = "OK",
    dismissText: String? = null,
    onConfirm: (() -> Unit)? = null,
    onDismissClick: (() -> Unit)? = null,
    isLoading: Boolean = false,
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = dialogShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Icon (optional)
                icon?.let { iconRes ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorApp.tintedSurface)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconRes,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Content (scrollable)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    content()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Loading indicator or Action buttons
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dismiss button (optional)
                        dismissText?.let { dismissLabel ->
                            TextButton(
                                onClick = onDismissClick ?: onDismiss,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = dismissLabel,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Confirm button
                        TextButton(
                            onClick = {
                                onConfirm?.invoke()
                                onDismiss()
                            },
                            modifier = Modifier
                        ) {
                            Text(
                                text = confirmText,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// Modern Alert Dialog - For Warnings/Errors
// ============================================================================

/**
 * Alert Dialog for warnings, errors, or important notices
 * 
 * Usage:
 * ```
 * ModernAlertDialog(
 *     onDismiss = { showAlert = false },
 *     alertType = AlertType.Error,
 *     title = "Error Occurred",
 *     message = "Something went wrong. Please try again.",
 *     confirmText = "Retry",
 *     onConfirm = { /* retry action */ }
 * )
 * ```
 */
@Composable
fun ModernAlertDialog(
    onDismiss: () -> Unit,
    alertType: AlertType = AlertType.Warning,
    title: String,
    message: String,
    confirmText: String = "OK",
    dismissText: String? = null,
    onConfirm: (() -> Unit)? = null,
    onDismissClick: (() -> Unit)? = null
) {
    ModernDialog(
        onDismiss = onDismiss,
        icon = alertType.icon,
        title = title,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
        onDismissClick = onDismissClick
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Alert types with corresponding icons and colors
 */
enum class AlertType(
    val icon: ImageVector,
    val color: @Composable () -> Color
) {
    Info(
        icon = Icons.Default.Info,
        color = { MaterialTheme.colorScheme.primary }
    ),
    Warning(
        icon = Icons.Default.Warning,
        color = { MaterialTheme.colorScheme.tertiary }
    ),
    Error(
        icon = Icons.Default.Error,
        color = { MaterialTheme.colorScheme.error }
    ),
    Success(
        icon = Icons.Default.CheckCircle,
        color = { my.noveldoksuha.coreui.theme.ColorSuccess }
    )
}

// ============================================================================
// Bottom Sheet Dialog - For Menus & Options
// ============================================================================

/**
 * Modern bottom sheet for menus and option panels
 * 
 * Usage:
 * ```
 * ModernBottomSheet(
 *     onDismiss = { showSheet = false },
 *     title = "Options"
 * ) {
 *     BottomSheetItem(
 *         icon = Icons.Default.Share,
 *         text = "Share",
 *         onClick = { /* share action */ }
 *     )
 *     BottomSheetItem(
 *         icon = Icons.Default.Delete,
 *         text = "Delete",
 *         onClick = { /* delete action */ },
 *         isDestructive = true
 *     )
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernBottomSheet(
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Title (optional)
            title?.let { sheetTitle ->
                Text(
                    text = sheetTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }

            content()
        }
    }
}

/**
 * Bottom sheet item for menu options
 */
@Composable
fun BottomSheetItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )
    }
}

// ============================================================================
// Confirmation Dialog - Quick Yes/No Questions
// ============================================================================

/**
 * Simple confirmation dialog for quick yes/no questions
 */
@Composable
fun ConfirmDialog(
    onDismiss: () -> Unit,
    title: String,
    message: String,
    icon: ImageVector? = null,
    confirmText: String = "Yes",
    dismissText: String = "No",
    onConfirm: () -> Unit,
    isDestructive: Boolean = false
) {
    ModernDialog(
        onDismiss = onDismiss,
        icon = icon,
        title = title,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
        onDismissClick = onDismiss
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
