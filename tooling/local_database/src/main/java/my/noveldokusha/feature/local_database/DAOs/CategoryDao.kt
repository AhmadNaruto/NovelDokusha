package my.noveldokusha.feature.local_database.DAOs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import my.noveldokusha.feature.local_database.tables.BookCategory
import my.noveldokusha.feature.local_database.tables.LibraryCategory

@Dao
interface CategoryDao {
    
    // Category CRUD
    @Query("SELECT * FROM library_categories ORDER BY createdAt ASC")
    fun getAllCategories(): Flow<List<LibraryCategory>>
    
    @Query("SELECT * FROM library_categories ORDER BY createdAt ASC")
    suspend fun getAllCategoriesList(): List<LibraryCategory>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: LibraryCategory): Long
    
    @Query("UPDATE library_categories SET name = :name WHERE id = :id")
    suspend fun updateCategoryName(id: Long, name: String)
    
    @Delete
    suspend fun deleteCategory(category: LibraryCategory)
    
    @Query("DELETE FROM library_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)
    
    // Book-Category mapping
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookToCategory(bookCategory: BookCategory)
    
    @Query("DELETE FROM book_categories WHERE bookUrl = :bookUrl AND categoryId = :categoryId")
    suspend fun removeBookFromCategory(bookUrl: String, categoryId: Long)
    
    @Query("SELECT categoryId FROM book_categories WHERE bookUrl = :bookUrl")
    suspend fun getCategoriesForBook(bookUrl: String): List<Long>
    
    @Query("SELECT categoryId FROM book_categories WHERE bookUrl = :bookUrl")
    fun getCategoriesForBookFlow(bookUrl: String): Flow<List<Long>>
    
    @Query("SELECT bookUrl FROM book_categories WHERE categoryId = :categoryId")
    suspend fun getBooksInCategory(categoryId: Long): List<String>
    
    @Transaction
    suspend fun setBookCategories(bookUrl: String, categoryIds: List<Long>) {
        // Remove existing categories
        removeBookFromAllCategories(bookUrl)
        // Add new categories
        categoryIds.forEach { categoryId ->
            addBookToCategory(BookCategory(bookUrl = bookUrl, categoryId = categoryId))
        }
    }
    
    @Query("DELETE FROM book_categories WHERE bookUrl = :bookUrl")
    suspend fun removeBookFromAllCategories(bookUrl: String)
    
    @Transaction
    suspend fun addBooksToCategory(bookUrls: List<String>, categoryId: Long) {
        bookUrls.forEach { bookUrl ->
            addBookToCategory(BookCategory(bookUrl = bookUrl, categoryId = categoryId))
        }
    }
    
    @Transaction
    suspend fun removeBooksFromCategory(bookUrls: List<String>, categoryId: Long) {
        bookUrls.forEach { bookUrl ->
            removeBookFromCategory(bookUrl, categoryId)
        }
    }
}
