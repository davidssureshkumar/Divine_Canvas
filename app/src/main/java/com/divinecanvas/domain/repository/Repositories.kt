package com.divinecanvas.domain.repository

import com.divinecanvas.core.AppResult
import com.divinecanvas.domain.model.BibleBook
import com.divinecanvas.domain.model.CanvasBackground
import com.divinecanvas.domain.model.Translation
import com.divinecanvas.domain.model.Verse

interface BibleRepository {
    /** All books with offline versification, ordered canonically. */
    suspend fun getBooks(): List<BibleBook>

    /** Resolve verse text: cache first, then free API, then offline fallback. */
    suspend fun getVerse(
        book: String,
        chapter: Int,
        verse: Int,
        translation: Translation,
    ): AppResult<Verse>

    /** Distinct theme names available for auto-pick. */
    suspend fun getThemes(): List<String>

    /** Pick a random verse relevant to [theme] and resolve its text. */
    suspend fun getRandomVerseForTheme(
        theme: String,
        translation: Translation,
    ): AppResult<Verse>
}

interface BackgroundRepository {
    /** True when at least one photo provider key is configured. */
    val photoSearchAvailable: Boolean

    /** Built-in offline gradients. */
    fun gradients(): List<CanvasBackground.Gradient>

    /** Search remote photos; empty/failure falls back to gradients in the UI. */
    suspend fun searchPhotos(query: String): AppResult<List<CanvasBackground.Photo>>
}
