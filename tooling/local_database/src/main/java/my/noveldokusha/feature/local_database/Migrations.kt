package my.noveldokusha.feature.local_database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import my.noveldokusha.feature.local_database.migrations.MigrationsList
import my.noveldokusha.feature.local_database.migrations._1stKissNovelDomainChange_1_org
import my.noveldokusha.feature.local_database.migrations.readLightNovelDomainChange_1_today
import my.noveldokusha.feature.local_database.migrations.readLightNovelDomainChange_2_meme

internal fun databaseMigrations() = arrayOf(
    migration(1) {
        it.execSQL("ALTER TABLE Chapter ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
    },
    migration(2) {
        it.execSQL("ALTER TABLE Book ADD COLUMN inLibrary INTEGER NOT NULL DEFAULT 0")
        it.execSQL("UPDATE Book SET inLibrary = 1")
    },
    migration(3) {
        it.execSQL("ALTER TABLE Book ADD COLUMN coverImageUrl TEXT NOT NULL DEFAULT ''")
        it.execSQL("ALTER TABLE Book ADD COLUMN description TEXT NOT NULL DEFAULT ''")
    },
    migration(4) {
        it.execSQL("ALTER TABLE Book ADD COLUMN lastReadEpochTimeMilli INTEGER NOT NULL DEFAULT 0")
    },
    migration(5, MigrationsList::readLightNovelDomainChange_1_today),
    migration(6, MigrationsList::readLightNovelDomainChange_2_meme),
    migration(7, MigrationsList::_1stKissNovelDomainChange_1_org),
    migration(8) {
        it.execSQL("""
            CREATE TABLE IF NOT EXISTS ChapterTranslation (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                chapterUrl TEXT NOT NULL,
                sourceLang TEXT NOT NULL,
                targetLang TEXT NOT NULL,
                originalText TEXT NOT NULL,
                translatedText TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """)
        it.execSQL("""
            CREATE INDEX IF NOT EXISTS index_ChapterTranslation_chapterUrl_sourceLang_targetLang
            ON ChapterTranslation (chapterUrl, sourceLang, targetLang)
        """)
    },
    migration(9) {
        // Create library_categories table
        it.execSQL("""
            CREATE TABLE IF NOT EXISTS library_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                colorIndex INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // Create book_categories mapping table
        it.execSQL("""
            CREATE TABLE IF NOT EXISTS book_categories (
                bookUrl TEXT NOT NULL,
                categoryId INTEGER NOT NULL,
                PRIMARY KEY (bookUrl, categoryId),
                FOREIGN KEY (bookUrl) REFERENCES Book(url) ON DELETE CASCADE,
                FOREIGN KEY (categoryId) REFERENCES library_categories(id) ON DELETE CASCADE
            )
        """)
        it.execSQL("CREATE INDEX IF NOT EXISTS index_book_categories_bookUrl ON book_categories (bookUrl)")
        it.execSQL("CREATE INDEX IF NOT EXISTS index_book_categories_categoryId ON book_categories (categoryId)")
        
        // Migrate existing completed books to a "Completed" category
        it.execSQL("INSERT OR IGNORE INTO library_categories (name, colorIndex, createdAt) VALUES ('Completed', 0, strftime('%s', 'now') * 1000)")
        it.execSQL("""
            INSERT OR IGNORE INTO book_categories (bookUrl, categoryId)
            SELECT url, (SELECT id FROM library_categories WHERE name = 'Completed')
            FROM Book
            WHERE completed = 1
        """)
    },
)

internal fun migration(vi: Int, migrate: (SupportSQLiteDatabase) -> Unit) =
    object : Migration(vi, vi + 1) {
        override fun migrate(db: SupportSQLiteDatabase) = migrate(db)
    }