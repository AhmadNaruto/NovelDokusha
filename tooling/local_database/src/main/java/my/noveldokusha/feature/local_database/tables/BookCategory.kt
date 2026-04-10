package my.noveldokusha.feature.local_database.tables

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "book_categories",
    primaryKeys = ["bookUrl", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["url"],
            childColumns = ["bookUrl"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LibraryCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookUrl"]),
        Index(value = ["categoryId"])
    ]
)
data class BookCategory(
    val bookUrl: String,
    val categoryId: Long
)
