package my.noveldokusha.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.theme.InternalTheme
import my.noveldoksuha.coreui.theme.PreviewThemes
import my.noveldoksuha.coreui.theme.Themes
import my.noveldokusha.settings.sections.AppUpdates
import my.noveldokusha.settings.sections.LibraryAutoUpdate
import my.noveldokusha.settings.sections.SettingsBackup
import my.noveldokusha.settings.sections.SettingsData
import my.noveldokusha.settings.sections.SettingsTheme
import my.noveldokusha.settings.sections.SettingsTranslationMethod
import my.noveldokusha.settings.sections.TranslationMethod

@Composable
internal fun SettingsScreenBody(
    state: SettingsScreenState,
    modifier: Modifier = Modifier,
    onFollowSystem: (Boolean) -> Unit,
    onThemeSelected: (Themes) -> Unit,
    onCleanDatabase: () -> Unit,
    onCleanImageFolder: () -> Unit,
    onBackupData: () -> Unit,
    onRestoreData: () -> Unit,
    onDownloadTranslationModel: (lang: String) -> Unit,
    onRemoveTranslationModel: (lang: String) -> Unit,
    onCheckForUpdatesManual: () -> Unit,
    onGeminiApiKeyChange: (String) -> Unit,
    onGeminiModelChange: (String) -> Unit,
    onPreferOnlineChange: (Boolean) -> Unit,
    onTranslationMethodChange: (TranslationMethod) -> Unit,
    mlKitStorageSize: Long = 0L,
) {
    // Determine current translation method based on settings
    val currentTranslationMethod = remember(
        state.geminiApiKey.value,
        state.preferOnlineTranslation.value
    ) {
        when {
            !state.preferOnlineTranslation.value -> TranslationMethod.MLKIT
            state.geminiApiKey.value.isNotBlank() -> TranslationMethod.GEMINI
            else -> TranslationMethod.GOOGLE_FREE
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        SettingsTheme(
            currentFollowSystem = state.followsSystemTheme.value,
            currentTheme = state.currentTheme.value,
            onFollowSystemChange = onFollowSystem,
            onCurrentThemeChange = onThemeSelected
        )
        HorizontalDivider()
        SettingsData(
            databaseSize = state.databaseSize.value,
            imagesFolderSize = state.imageFolderSize.value,
            onCleanDatabase = onCleanDatabase,
            onCleanImageFolder = onCleanImageFolder
        )
        HorizontalDivider()
        SettingsBackup(
            onBackupData = onBackupData,
            onRestoreData = onRestoreData
        )
        if (state.isTranslationSettingsVisible.value) {
            HorizontalDivider()
            SettingsTranslationMethod(
                geminiApiKey = state.geminiApiKey.value,
                geminiModel = state.geminiModel.value,
                preferOnlineTranslation = state.preferOnlineTranslation.value,
                currentTranslationMethod = currentTranslationMethod,
                mlKitStorageSize = mlKitStorageSize,
                onGeminiApiKeyChange = onGeminiApiKeyChange,
                onGeminiModelChange = onGeminiModelChange,
                onPreferOnlineChange = onPreferOnlineChange,
                onTranslationMethodChange = onTranslationMethodChange
            )
        }
        HorizontalDivider()
        LibraryAutoUpdate(state = state.libraryAutoUpdate)
        HorizontalDivider()
        AppUpdates(
            state = state.updateAppSetting,
            onCheckForUpdatesManual = onCheckForUpdatesManual
        )
        Spacer(modifier = Modifier.height(500.dp))
        Text(
            text = "(°.°)",
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(120.dp))
    }
}


@PreviewThemes
@Composable
private fun Preview() {
    val isDark = isSystemInDarkTheme()
    val theme = remember { mutableStateOf(if (isDark) Themes.DARK else Themes.LIGHT) }
    InternalTheme(theme.value) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreenBody(
                state = SettingsScreenState(
                    followsSystemTheme = remember { derivedStateOf { true } },
                    currentTheme = theme,
                    databaseSize = remember { mutableStateOf("1 MB") },
                    imageFolderSize = remember { mutableStateOf("10 MB") },
                    isTranslationSettingsVisible = remember { mutableStateOf(true) },
                    translationModelsStates = remember { mutableStateListOf() },
                    updateAppSetting = SettingsScreenState.UpdateApp(
                        currentAppVersion = "1.0.0",
                        appUpdateCheckerEnabled = remember { mutableStateOf(true) },
                        showNewVersionDialog = remember {
                            mutableStateOf(
                                null
                            )
                        },
                        checkingForNewVersion = remember { mutableStateOf(true) },
                    ),
                    libraryAutoUpdate = SettingsScreenState.LibraryAutoUpdate(
                        autoUpdateEnabled = remember { mutableStateOf(true) },
                        autoUpdateIntervalHours = remember { mutableIntStateOf(24) },
                    ),
                    geminiApiKey = remember { derivedStateOf { "" } },
                    geminiModel = remember { derivedStateOf { "" } },
                    preferOnlineTranslation = remember { derivedStateOf { false } },
                    currentTranslationMethod = remember { mutableStateOf(TranslationMethod.GOOGLE_FREE) },
                    mlKitStorageSize = remember { mutableStateOf(0L) },
                ),
                onFollowSystem = { },
                onThemeSelected = { },
                onCleanDatabase = { },
                onCleanImageFolder = { },
                onBackupData = { },
                onRestoreData = { },
                onDownloadTranslationModel = { },
                onRemoveTranslationModel = { },
                onCheckForUpdatesManual = { },
                onGeminiApiKeyChange = { },
                onGeminiModelChange = { },
                onPreferOnlineChange = { },
                onTranslationMethodChange = { },
                mlKitStorageSize = 0L,
            )
        }
    }
}
