package my.noveldokusha.settings.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import my.noveldoksuha.coreui.components.SaltSettingsSection
import my.noveldoksuha.coreui.theme.ColorAccent
import my.noveldoksuha.coreui.theme.textPadding
import my.noveldokusha.settings.R

/**
 * Translation method selection enum
 */
enum class TranslationMethod(val displayName: String, val description: String) {
    GEMINI("Google Gemini API", "Online translation with API key (highest quality)"),
    GOOGLE_FREE("Google Translate (Free)", "Online translation, no API key required"),
    MLKIT("On-Device (MLKit)", "Offline translation, requires model download")
}

@Composable
internal fun SettingsTranslationMethod(
    geminiApiKey: String,
    geminiModel: String,
    preferOnlineTranslation: Boolean,
    currentTranslationMethod: TranslationMethod,
    mlKitStorageSize: Long,
    onGeminiApiKeyChange: (String) -> Unit,
    onGeminiModelChange: (String) -> Unit,
    onPreferOnlineChange: (Boolean) -> Unit,
    onTranslationMethodChange: (TranslationMethod) -> Unit,
) {
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showTranslationMethodDialog by remember { mutableStateOf(false) }

    SaltSettingsSection(
        title = "Translation Settings",
        tip = "Choose your preferred translation method"
    ) {
        // Translation Method Selection
        ListItem(
            headlineContent = {
                Text(
                    text = "Translation Method",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Text(
                    text = currentTranslationMethod.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            leadingContent = {
                Icon(
                    when (currentTranslationMethod) {
                        TranslationMethod.GEMINI -> Icons.Outlined.Api
                        TranslationMethod.GOOGLE_FREE -> Icons.Outlined.Cloud
                        TranslationMethod.MLKIT -> Icons.Outlined.PhoneAndroid
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentTranslationMethod == TranslationMethod.MLKIT) {
                            formatStorageSize(mlKitStorageSize)
                        } else {
                            when {
                                currentTranslationMethod == TranslationMethod.GEMINI && geminiApiKey.isNotBlank() -> "✓ API Key Set"
                                currentTranslationMethod == TranslationMethod.GEMINI -> "⚠ No API Key"
                                else -> "Online"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            currentTranslationMethod == TranslationMethod.GEMINI && geminiApiKey.isNotBlank() -> MaterialTheme.colorScheme.primary
                            currentTranslationMethod == TranslationMethod.GEMINI -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier.clickable { showTranslationMethodDialog = true }
        )

        // Show method-specific settings
        when (currentTranslationMethod) {
            TranslationMethod.GEMINI -> {
                // Gemini API Key setting
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Gemini API Key",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    supportingContent = {
                        Text(
                            text = if (geminiApiKey.isNotBlank()) "API key configured" else "No API key set",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Key,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { showApiKeyDialog = true }
                )

                // Gemini Model setting
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Gemini Model",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    supportingContent = {
                        Text(
                            text = geminiModel.ifBlank { "gemini-2.0-flash-exp (default)" },
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier.clickable {
                        // Could add model selection dialog here
                    }
                )
            }
            TranslationMethod.MLKIT -> {
                // MLKit storage info
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Storage Used",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    supportingContent = {
                        Text(
                            text = formatStorageSize(mlKitStorageSize),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.PhoneAndroid,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
            TranslationMethod.GOOGLE_FREE -> {
                // Info about free translation
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Translation Limit",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "~13k characters per request",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Cloud,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }

    // Translation Method Selection Dialog
    if (showTranslationMethodDialog) {
        TranslationMethodDialog(
            currentMethod = currentTranslationMethod,
            geminiApiKey = geminiApiKey,
            onMethodSelected = {
                onTranslationMethodChange(it)
                when (it) {
                    TranslationMethod.GEMINI -> onPreferOnlineChange(true)
                    TranslationMethod.GOOGLE_FREE -> onPreferOnlineChange(true)
                    TranslationMethod.MLKIT -> onPreferOnlineChange(false)
                }
                showTranslationMethodDialog = false
            },
            onDismiss = { showTranslationMethodDialog = false }
        )
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        ApiKeyDialog(
            apiKey = geminiApiKey,
            model = geminiModel,
            onApiKeyChange = onGeminiApiKeyChange,
            onModelChange = onGeminiModelChange,
            onDismiss = { showApiKeyDialog = false }
        )
    }
}

@Composable
private fun TranslationMethodDialog(
    currentMethod: TranslationMethod,
    geminiApiKey: String,
    onMethodSelected: (TranslationMethod) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Translation Method")
        },
        text = {
            Column {
                TranslationMethod.RadioButtons(
                    selectedMethod = currentMethod,
                    geminiApiKey = geminiApiKey,
                    onOptionSelected = onMethodSelected
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun TranslationMethod.RadioButtons(
    selectedMethod: TranslationMethod,
    geminiApiKey: String,
    onOptionSelected: (TranslationMethod) -> Unit
) {
    Column {
        TranslationMethod.entries.forEach { method ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(method) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = method == selectedMethod,
                    onClick = { onOptionSelected(method) }
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = method.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (method == selectedMethod)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = method.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (method == TranslationMethod.GEMINI && geminiApiKey.isBlank()) {
                        Text(
                            text = "⚠ API Key required - configure in settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiKeyDialog(
    apiKey: String,
    model: String,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyText by remember(apiKey) { mutableStateOf(apiKey) }
    var modelText by remember(model) { mutableStateOf(model) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Gemini API Configuration")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    label = { Text("API Key") },
                    placeholder = { Text("Enter your Gemini API key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = modelText,
                    onValueChange = { modelText = it },
                    label = { Text("Model") },
                    placeholder = { Text("gemini-2.0-flash-exp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Get your API key from Google AI Studio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onApiKeyChange(apiKeyText)
                onModelChange(modelText)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatStorageSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
