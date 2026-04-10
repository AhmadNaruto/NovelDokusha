package my.noveldoksuha.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import my.noveldokusha.feature.local_database.AppDatabase
import my.noveldokusha.feature.local_database.tables.BookCategory
import my.noveldokusha.feature.local_database.tables.LibraryCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val db: AppDatabase
) {
    private val dao get() = db.categoryDao()

    fun getAllCategoriesFlow(): Flow<List<LibraryCategory>> = dao.getAllCategories()

    suspend fun getAllCategories(): List<LibraryCategory> =
        withContext(Dispatchers.IO) { dao.getAllCategoriesList() }

    suspend fun createCategory(name: String): Long =
        withContext(Dispatchers.IO) {
            dao.insertCategory(LibraryCategory(name = name))
        }

    suspend fun updateCategory(id: Long, name: String) =
        withContext(Dispatchers.IO) { dao.updateCategoryName(id, name) }

    suspend fun deleteCategory(category: LibraryCategory) =
        withContext(Dispatchers.IO) { dao.deleteCategory(category) }

    suspend fun getCategoriesForBook(bookUrl: String): List<Long> =
        withContext(Dispatchers.IO) { dao.getCategoriesForBook(bookUrl) }

    fun getCategoriesForBookFlow(bookUrl: String): Flow<List<Long>> =
        dao.getCategoriesForBookFlow(bookUrl)

    suspend fun setBookCategories(bookUrl: String, categoryIds: List<Long>) =
        withContext(Dispatchers.IO) { dao.setBookCategories(bookUrl, categoryIds) }

    suspend fun addBookToCategories(bookUrl: String, categoryIds: List<Long>) =
        withContext(Dispatchers.IO) {
            val existing = dao.getCategoriesForBook(bookUrl)
            val newCategories = (existing + categoryIds).distinct()
            dao.setBookCategories(bookUrl, newCategories)
        }

    suspend fun removeBookFromCategory(bookUrl: String, categoryId: Long) =
        withContext(Dispatchers.IO) { dao.removeBookFromCategory(bookUrl, categoryId) }

    suspend fun getBooksInCategory(categoryId: Long): List<String> =
        withContext(Dispatchers.IO) { dao.getBooksInCategory(categoryId) }

    suspend fun addMultipleBooksToCategory(bookUrls: List<String>, categoryId: Long) =
        withContext(Dispatchers.IO) { dao.addBooksToCategory(bookUrls, categoryId) }

    suspend fun removeMultipleBooksFromCategory(bookUrls: List<String>, categoryId: Long) =
        withContext(Dispatchers.IO) { dao.removeBooksFromCategory(bookUrls, categoryId) }
}
