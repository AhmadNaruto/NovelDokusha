package my.noveldokusha.features.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import dagger.hilt.android.AndroidEntryPoint
import my.noveldoksuha.coreui.BaseActivity
import my.noveldoksuha.coreui.components.AnimatedTransition
import my.noveldoksuha.coreui.theme.Theme
import my.noveldokusha.R
import my.noveldokusha.catalogexplorer.CatalogExplorerScreen
import my.noveldokusha.libraryexplorer.LibraryScreen
import my.noveldokusha.settings.SettingsScreen

private data class Page(
    @DrawableRes val iconRes: Int,
    @StringRes val stringRes: Int,
)

private val pages = listOf(
    Page(iconRes = R.drawable.ic_baseline_home_24, stringRes = R.string.title_library),
    Page(iconRes = R.drawable.ic_baseline_menu_book_24, stringRes = R.string.title_finder),
    Page(iconRes = R.drawable.ic_twotone_settings_24, stringRes = R.string.title_settings),
)


@OptIn(ExperimentalAnimationApi::class)
@AndroidEntryPoint
open class MainActivity : BaseActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPushNotificationPermission()

        setContent {
            var activePageIndex by rememberSaveable { mutableIntStateOf(0) }

            BackHandler(enabled = activePageIndex != 0) {
                activePageIndex = 0
            }

            Theme(themeProvider = themeProvider) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize()) {
                        AnimatedTransition(targetState = activePageIndex) {
                            when (it) {
                                0 -> LibraryScreen()
                                1 -> CatalogExplorerScreen()
                                2 -> SettingsScreen()
                            }
                        }
                    }
                    
                    // Floating Bottom Navigation Bar
                    Surface(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        NavigationBar(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            tonalElevation = 0.dp,
                        ) {
                            pages.forEachIndexed { pageIndex, page ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = page.iconRes),
                                            contentDescription = stringResource(id = page.stringRes)
                                        )
                                    },
                                    label = { Text(stringResource(id = page.stringRes)) },
                                    selected = activePageIndex == pageIndex,
                                    onClick = {
                                        activePageIndex = pageIndex
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        handleIntent(intent)
    }

    private fun requestPushNotificationPermission() {
        // check if sdk level is more than 33
        if (VERSION.SDK_INT < VERSION_CODES.TIRAMISU) {
            return
        }

        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        if (result != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun handleIntent(intent: Intent) {
        // EPUB handling has been removed since Local source and EPUB import are no longer supported
    }
}

