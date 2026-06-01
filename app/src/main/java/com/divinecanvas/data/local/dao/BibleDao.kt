package com.divinecanvas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.divinecanvas.data.local.entity.BookEntity
import com.divinecanvas.data.local.entity.ThemeVerseEntity
import com.divinecanvas.data.local.entity.VerseEntity

@Dao
interface BibleDao {

    // --- Books / versification ---
    @Query("SELECT COUNT(*) FROM books")
    suspend fun bookCount(): Int

    @Query("SELECT * FROM books ORDER BY `order` ASC")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("SELECT * FROM books WHERE name = :name LIMIT 1")
    suspend fun getBookByName(name: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    // --- Cached verse text ---
    @Query(
        "SELECT * FROM verses WHERE book = :book AND chapter = :chapter " +
            "AND verse = :verse AND translation = :translation LIMIT 1"
    )
    suspend fun getCachedVerse(
        book: String,
        chapter: Int,
        verse: Int,
        translation: String,
    ): VerseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheVerse(verse: VerseEntity)

    // --- Themes ---
    @Query("SELECT DISTINCT theme FROM theme_verses ORDER BY theme ASC")
    suspend fun getThemeNames(): List<String>

    @Query("SELECT COUNT(*) FROM theme_verses")
    suspend fun themeVerseCount(): Int

    @Query("SELECT * FROM theme_verses WHERE theme = :theme")
    suspend fun getVersesForTheme(theme: String): List<ThemeVerseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThemeVerses(verses: List<ThemeVerseEntity>)
}
