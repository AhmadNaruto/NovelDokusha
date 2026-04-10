package my.noveldokusha.libraryexplorer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import my.noveldoksuha.coreui.BaseViewModel
import my.noveldoksuha.coreui.components.BookSettingsDialogState
import my.noveldoksuha.data.AppRepository
import my.noveldoksuha.data.CategoryRepository
import my.noveldokusha.core.Toasty
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.core.appPreferences.TernaryState
import my.noveldokusha.core.utils.toState
import my.noveldokusha.feature.local_database.BookWithContext
import my.noveldokusha.feature.local_database.tables.LibraryCategory
import javax.inject.Inject

@HiltViewModel
internal class LibraryPageViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val categoryRepository: CategoryRepository,
    private val preferences: AppPreferences,
    private val toasty: Toasty,
) : BaseViewModel() {
    var isPullRefreshing by mutableStateOf(false)
    var selectedCategoryId by mutableStateOf(-1L)
    var isInMultiSelectMode by mutableStateOf(false)
    var showBottomSheet by mutableStateOf(false)
    var bookSettingsDialogState by mutableStateOf<BookSettingsDialogState>(BookSettingsDialogState.Hide)
    val selectedBooksForEdit = mutableSetOf<String>()

    val categoriesFlow = categoryRepository.getAllCategoriesFlow()
        .toState(viewModelScope, emptyList())

    private fun getBooksByCategory(categoryId: Long): Flow<List<BookWithContext>> {
        return appRepository.libraryBooks.getBooksInLibraryWithContextFlow
            .combine(preferences.LIBRARY_FILTER_READ.flow()) { list, filterRead ->
                val filteredByRead = when (filterRead) {
                    TernaryState.Active -> list.filter { it.chaptersCount == it.chaptersReadCount }
                    TernaryState.Inverse -> list.filter { it.chaptersCount != it.chaptersReadCount }
                    else -> list
                }
                filteredByRead
            }.combine(preferences.LIBRARY_SORT_LAST_READ.flow()) { list, sortRead ->
                when (sortRead) {
                    TernaryState.Active -> list.sortedByDescending { it.book.lastReadEpochTimeMilli }
                    TernaryState.Inverse -> list.sortedBy { it.book.lastReadEpochTimeMilli }
                    else -> list
                }
            }.flatMapLatest { list ->
                if (categoryId == -1L) {
                    kotlinx.coroutines.flow.flowOf(list)
                } else {
                    kotlinx.coroutines.flow.flow {
                        val bookUrls = categoryRepository.getBooksInCategory(categoryId)
                        emit(list.filter { it.book.url in bookUrls })
                    }
                }
            }
    }

    val filteredBooks = getBooksByCategory(selectedCategoryId)
        .toState(viewModelScope, listOf())

    suspend fun createCategory(name: String): Long {
        return categoryRepository.createCategory(name)
    }

    suspend fun deleteCategory(category: LibraryCategory) {
        categoryRepository.deleteCategory(category)
    }

    suspend fun updateCategory(id: Long, name: String) {
        categoryRepository.updateCategory(id, name)
    }

    suspend fun setBookCategories(bookUrl: String, categoryIds: List<Long>) {
        categoryRepository.setBookCategories(bookUrl, categoryIds)
    }

    suspend fun addBooksToCategories(bookUrls: List<String>, categoryIds: List<Long>) {
        categoryIds.forEach { categoryId ->
            categoryRepository.addMultipleBooksToCategory(bookUrls, categoryId)
        }
    }

    suspend fun getCategoriesForBook(bookUrl: String): List<Long> {
        return categoryRepository.getCategoriesForBook(bookUrl)
    }

    fun getBook(bookUrl: String) = appRepository.libraryBooks.getFlow(bookUrl)

    fun bookCompletedToggle(bookUrl: String) {
        viewModelScope.launch {
            val book = appRepository.libraryBooks.get(bookUrl) ?: return@launch
            appRepository.libraryBooks.update(book.copy(completed = !book.completed))
        }
    }

    fun toggleMultiSelectMode() {
        isInMultiSelectMode = !isInMultiSelectMode
        if (!isInMultiSelectMode) selectedBooksForEdit.clear()
    }

    fun toggleBookSelection(bookUrl: String) {
        if (selectedBooksForEdit.contains(bookUrl)) {
            selectedBooksForEdit.remove(bookUrl)
        } else {
            selectedBooksForEdit.add(bookUrl)
        }
    }

    fun selectAllBooks() {
        selectedBooksForEdit.clear()
        selectedBooksForEdit.addAll(filteredBooks.value.map { it.book.url })
    }

    private fun showLoadingSpinner() {
        viewModelScope.launch {
            isPullRefreshing = true
            delay(3000L)
            isPullRefreshing = false
        }
    }

    fun onRefresh() {
        showLoadingSpinner()
        toasty.show(R.string.updating_library_notice)
    }
}
