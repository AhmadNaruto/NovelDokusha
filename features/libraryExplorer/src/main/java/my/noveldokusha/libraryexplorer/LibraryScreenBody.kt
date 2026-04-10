package my.noveldokusha.libraryexplorer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import my.noveldoksuha.coreui.components.CollapsibleDivider
import my.noveldoksuha.coreui.theme.colorApp
import my.noveldokusha.core.domain.LibraryCategory
import my.noveldokusha.feature.local_database.BookWithContext

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
internal fun LibraryScreenBody(
    innerPadding: PaddingValues,
    topAppBarState: TopAppBarState,
    onBookClick: (BookWithContext) -> Unit,
    onBookLongClick: (BookWithContext) -> Unit,
    viewModel: LibraryPageViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    var selectedFilter by remember { mutableIntStateOf(0) }
    
    val filterOptions = listOf(
        FilterOption("All", Icons.Default.AllInclusive, LibraryCategory.DEFAULT),
        FilterOption("Reading", Icons.Default.MenuBook, LibraryCategory.DEFAULT),
        FilterOption("Completed", Icons.Default.CheckCircle, LibraryCategory.COMPLETED)
    )

    val pullRefreshState = rememberPullRefreshState(
        refreshing = viewModel.isPullRefreshing,
        onRefresh = {
            val category = when (selectedFilter) {
                2 -> LibraryCategory.COMPLETED
                else -> LibraryCategory.DEFAULT
            }
            viewModel.onLibraryCategoryRefresh(libraryCategory = category)
        }
    )

    Box(
        modifier = Modifier
            .pullRefresh(state = pullRefreshState)
            .padding(innerPadding)
    ) {
        Column {
            // Modern Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = selectedFilter == filterOptions.indexOf(filter),
                        onClick = { 
                            selectedFilter = filterOptions.indexOf(filter)
                            viewModel.onLibraryCategoryRefresh(
                                libraryCategory = filter.category
                            )
                        },
                        label = { Text(filter.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = filter.icon,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        },
                        modifier = Modifier.animateContentSize()
                    )
                }
            }

            CollapsibleDivider(topAppBarState)

            // Book Grid
            val list: List<BookWithContext> by remember {
                derivedStateOf {
                    when (selectedFilter) {
                        0 -> viewModel.listReading // All
                        1 -> viewModel.listReading.filter { it.chaptersReadCount < it.chaptersCount } // Reading
                        2 -> viewModel.listCompleted // Completed
                        else -> viewModel.listReading
                    }
                }
            }
            
            LibraryPageBody(
                list = list,
                onClick = onBookClick,
                onLongClick = onBookLongClick
            )
        }
        
        PullRefreshIndicator(
            refreshing = viewModel.isPullRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

/**
 * Filter option data class
 */
private data class FilterOption(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val category: LibraryCategory
)
