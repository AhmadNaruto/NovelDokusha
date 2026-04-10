package my.noveldokusha.libraryexplorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import my.noveldoksuha.coreui.components.CollapsibleDivider
import my.noveldokusha.feature.local_database.BookWithContext
import my.noveldokusha.feature.local_database.tables.LibraryCategory
import my.noveldokusha.libraryexplorer.LibraryPageBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreenBody(
    innerPadding: PaddingValues,
    topAppBarState: TopAppBarState,
    onBookClick: (BookWithContext) -> Unit,
    onBookLongClick: (BookWithContext) -> Unit,
    viewModel: LibraryPageViewModel = viewModel()
) {
    val categories by remember { viewModel.categoriesFlow }
    val books by remember { viewModel.filteredBooks }
    val selectedCategory = viewModel.selectedCategoryId

    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = viewModel.isPullRefreshing,
        onRefresh = { viewModel.onRefresh() },
        state = pullRefreshState,
        modifier = Modifier.padding(innerPadding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                CategoryFilterChips(
                    categories = categories,
                    selectedCategoryId = selectedCategory,
                    onCategorySelected = { viewModel.selectedCategoryId = it }
                )
            }
            item {
                CollapsibleDivider(topAppBarState)
            }
            item {
                BookGrid(
                    books = books,
                    onBookClick = onBookClick,
                    onBookLongClick = onBookLongClick
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterChips(
    categories: List<LibraryCategory>,
    selectedCategoryId: Long,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == -1L,
                onClick = { onCategorySelected(-1L) },
                label = { Text("All") },
                leadingIcon = {
                    Icon(Icons.Default.AllInclusive, null)
                }
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) }
            )
        }
    }
}

@Composable
private fun BookGrid(
    books: List<BookWithContext>,
    onBookClick: (BookWithContext) -> Unit,
    onBookLongClick: (BookWithContext) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp, start = 4.dp, end = 4.dp),
        modifier = modifier.height((books.size / 2 * 250).dp.coerceAtLeast(400.dp))
    ) {
        // Book items will be rendered here
        // This is a placeholder - actual implementation needs LibraryPageBody integration
    }
}
