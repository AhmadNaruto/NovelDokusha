package my.noveldokusha.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import my.noveldokusha.strings.R

@Composable
internal fun SettingsUserAgent(
    currentUserAgent: String,
    defaultUserAgent: String,
    onUserAgentChange: (String) -> Unit,
) {
    var textFieldValue by remember { mutableStateOf(currentUserAgent) }

    // Sync local state when external value changes (e.g. from SharedPreferences)
    if (textFieldValue != currentUserAgent) {
        textFieldValue = currentUserAgent
    }

    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.settings_user_agent_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = stringResource(R.string.settings_user_agent_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var textFieldValue by remember { mutableStateOf(currentUserAgent) }

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onUserAgentChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = defaultUserAgent,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            trailingIcon = {
                if (textFieldValue.isNotBlank()) {
                    IconButton(onClick = {
                        textFieldValue = ""
                        onUserAgentChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.settings_user_agent_reset)
                        )
                    }
                }
            },
            singleLine = true,
            maxLines = 1,
        )

        if (textFieldValue.isNotBlank()) {
            Text(
                text = "${stringResource(R.string.settings_user_agent_active)} $textFieldValue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Text(
                text = "${stringResource(R.string.settings_user_agent_default)} $defaultUserAgent",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
internal fun SettingsUserAgentPreview() {
    SettingsUserAgent(
        currentUserAgent = "",
        defaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64)",
        onUserAgentChange = {},
    )
}
