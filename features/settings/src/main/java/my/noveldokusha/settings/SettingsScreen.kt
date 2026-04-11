package my.noveldokusha.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import my.noveldoksuha.coreui.components.CollapsibleDivider
import my.noveldokusha.tooling.backup_create.onBackupCreate
import my.noveldokusha.tooling.backup_restore.onBackupRestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = viewModel()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                SettingsTopAppBar(scrollBehavior = scrollBehavior)
                CollapsibleDivider(scrollBehavior.state)
            }
        },
        content = { innerPadding ->
            SettingsScreenBody(
                state = viewModel.state,
                onFollowSystem = viewModel::onFollowSystemChange,
                onThemeSelected = viewModel::onThemeChange,
                onCleanDatabase = viewModel::cleanDatabase,
                onCleanImageFolder = viewModel::cleanImagesFolder,
                onBackupData = onBackupCreate(),
                onRestoreData = onBackupRestore(),
                onDownloadTranslationModel = viewModel::downloadTranslationModel,
                onRemoveTranslationModel = viewModel::removeTranslationModel,
                onCheckForUpdatesManual = viewModel::onCheckForUpdatesManual,
                onGeminiApiKeyChange = viewModel::onGeminiApiKeyChange,
                onGeminiModelChange = viewModel::onGeminiModelChange,
                onPreferOnlineChange = viewModel::onPreferOnlineTranslationChange,
                onPreferOfflineChange = viewModel::onPreferOfflineTranslationChange,
                onUserAgentChange = viewModel::onUserAgentChange,
                modifier = Modifier.padding(innerPadding),
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar(
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        title = { }
    )
}

