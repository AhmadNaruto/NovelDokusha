package my.noveldokusha.libraryexplorer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import my.noveldoksuha.coreui.components.BookSettingsDialog
import my.noveldoksuha.coreui.components.BookSettingsDialogState
import my.noveldoksuha.coreui.theme.colorApp
import my.noveldokusha.navigation.NavigationRouteViewModel
import my.noveldokusha.feature.local_database.BookMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navigationRouteViewModel: NavigationRouteViewModel = viewModel()
) {
    val libraryModel: LibraryPageViewModel = viewModel()

    val context by rememberUpdatedState(LocalContext.current)
    var showDropDown by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LibraryTopAppBar(
                scrollBehavior = scrollBehavior,
                showSearch = showSearch,
                showManageCategories = showManageCategories,
                onSearchToggle = { showSearch = !showSearch },
                onManageCategoriesToggle = { showManageCategories = !showManageCategories },
                onFilterClick = { libraryModel.showBottomSheet = !libraryModel.showBottomSheet },
                showDropDown = showDropDown,
                onDropDownToggle = { showDropDown = !showDropDown },
                onDismissDropDown = { showDropDown = false }
            )
        },
        content = { innerPadding ->
            LibraryScreenBody(
                innerPadding = innerPadding,
                topAppBarState = scrollBehavior.state,
                onBookClick = { book ->
                    navigationRouteViewModel.chapters(
                        context = context,
                        bookMetadata = BookMetadata(
                            title = book.book.title,
                            url = book.book.url
                        )
                    ).let(context::startActivity)
                },
                onBookLongClick = {
                    libraryModel.bookSettingsDialogState = BookSettingsDialogState.Show(it.book)
                }
            )
        }
    )

    // Book selected options dialog
    when (val state = libraryModel.bookSettingsDialogState) {
        is BookSettingsDialogState.Show -> {

            BookSettingsDialog(
                book = libraryModel.getBook(state.book.url)
                    .collectAsState(initial = state.book)
                    .value ?: state.book,
                onDismiss = { libraryModel.bookSettingsDialogState = BookSettingsDialogState.Hide },
                onToggleCompleted = { libraryModel.bookCompletedToggle(state.book.url) }
            )
        }

        else -> Unit
    }

    LibraryBottomSheet(
        visible = libraryModel.showBottomSheet,
        onDismiss = { libraryModel.showBottomSheet = false }
    )

    if (showManageCategories) {
        ManageCategoriesDialog(
            viewModel = libraryModel,
            onDismiss = { showManageCategories = false }
        )
    }
}

/**
 * Modern Library TopAppBar with gradient background
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopAppBar(
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    showSearch: Boolean,
    showManageCategories: Boolean,
    onSearchToggle: () -> Unit,
    onManageCategoriesToggle: () -> Unit,
    onFilterClick: () -> Unit,
    showDropDown: Boolean,
    onDropDownToggle: () -> Unit,
    onDismissDropDown: () -> Unit
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        title = { },
        actions = {
            // Search toggle
            IconButton(onClick = onSearchToggle) {
                Icon(
                    Icons.Filled.Search,
                    "Search"
                )
            }
            
            // Filter
            IconButton(onClick = onFilterClick) {
                Icon(
                    Icons.Filled.FilterList,
                    stringResource(R.string.filter),
                    tint = MaterialTheme.colorApp.badgeBackground
                )
            }
            
            // More options dropdown
            IconButton(onClick = onDropDownToggle) {
                Icon(
                    Icons.Filled.MoreVert,
                    stringResource(R.string.options_panel)
                )
            }
            LibraryDropDown(
                expanded = showDropDown,
                onDismiss = onDismissDropDown,
                onManageCategories = onManageCategoriesToggle
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}
