package my.noveldokusha.feature.local_database.tables

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "library_categories")
data class LibraryCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorIndex: Int = 0, // For future color customization
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        const val ALL_CATEGORY_ID = -1L // Special ID for "All" virtual category
    }
}
