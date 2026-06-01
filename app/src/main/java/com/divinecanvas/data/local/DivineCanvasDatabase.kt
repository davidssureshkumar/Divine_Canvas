package com.divinecanvas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.divinecanvas.data.local.dao.BibleDao
import com.divinecanvas.data.local.entity.BookEntity
import com.divinecanvas.data.local.entity.ThemeVerseEntity
import com.divinecanvas.data.local.entity.VerseEntity

@Database(
    entities = [BookEntity::class, VerseEntity::class, ThemeVerseEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class DivineCanvasDatabase : RoomDatabase() {
    abstract fun bibleDao(): BibleDao

    companion object {
        const val NAME = "divine_canvas.db"
    }
}
