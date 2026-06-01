package com.divinecanvas.data.local.seed

import android.content.Context
import com.divinecanvas.data.local.dao.BibleDao
import com.divinecanvas.data.local.entity.BookEntity
import com.divinecanvas.data.local.entity.ThemeVerseEntity
import com.divinecanvas.data.local.entity.VerseEntity
import com.divinecanvas.domain.model.Translation
import kotlinx.serialization.json.Json

/**
 * Populates the Room database from bundled JSON assets the first time the app runs
 * (or whenever the tables are empty). This keeps all versification and theme data
 * fully offline while remaining trivially editable.
 */
class BibleSeeder(
    private val context: Context,
    private val json: Json,
) {
    suspend fun seedIfNeeded(dao: BibleDao) {
        if (dao.bookCount() == 0) seedBooks(dao)
        if (dao.themeVerseCount() == 0) seedThemes(dao)
    }

    private suspend fun seedBooks(dao: BibleDao) {
        val raw = readAsset("bible/versification.json")
        val parsed = json.decodeFromString(VersificationFile.serializer(), raw)
        val books = parsed.books.map { b ->
            BookEntity(
                order = b.order,
                name = b.name,
                abbrev = b.abbrev,
                testament = b.testament,
                verseCountsCsv = b.verses.joinToString(","),
            )
        }
        dao.insertBooks(books)
    }

    private suspend fun seedThemes(dao: BibleDao) {
        val raw = readAsset("bible/themes.json")
        val parsed = json.decodeFromString(ThemesFile.serializer(), raw)
        val translation = Translation.fromId(parsed.translation.lowercase()).id

        val themeVerses = mutableListOf<ThemeVerseEntity>()
        for (theme in parsed.themes) {
            for (v in theme.verses) {
                themeVerses += ThemeVerseEntity(
                    theme = theme.name,
                    book = v.book,
                    chapter = v.chapter,
                    verse = v.verse,
                )
                // Cache the curated text so theme verses (and these references) work offline.
                dao.cacheVerse(
                    VerseEntity(
                        book = v.book,
                        chapter = v.chapter,
                        verse = v.verse,
                        translation = translation,
                        reference = v.reference,
                        text = v.text,
                    )
                )
            }
        }
        dao.insertThemeVerses(themeVerses)
    }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }
}
