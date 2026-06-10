package com.divinecanvas.data.repository

import com.divinecanvas.core.AppResult
import com.divinecanvas.data.local.dao.BibleDao
import com.divinecanvas.data.local.entity.BookEntity
import com.divinecanvas.data.local.entity.VerseEntity
import com.divinecanvas.data.local.kjv.KjvOfflineSource
import com.divinecanvas.data.remote.api.BibleApi
import com.divinecanvas.di.IoDispatcher
import com.divinecanvas.domain.model.BibleBook
import com.divinecanvas.domain.model.Testament
import com.divinecanvas.domain.model.Translation
import com.divinecanvas.domain.model.Verse
import com.divinecanvas.domain.repository.BibleRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class BibleRepositoryImpl
@Inject
constructor(
    private val dao: BibleDao,
    private val bibleApi: BibleApi,
    private val kjvSource: KjvOfflineSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BibleRepository {

    override suspend fun getBooks(): List<BibleBook> =
        withContext(ioDispatcher) { dao.getAllBooks().map { it.toDomain() } }

    override suspend fun getVerse(
        book: String,
        chapter: Int,
        verse: Int,
        translation: Translation,
    ): AppResult<Verse> =
        withContext(ioDispatcher) {
            val reference = "$book $chapter:$verse"

            // 1. Cache-first (seeded theme verses + anything resolved previously).
            dao.getCachedVerse(book, chapter, verse, translation.id)?.let {
                return@withContext AppResult.Success(it.toVerse(translation), fromCache = false)
            }

            val bookEntity = dao.getBookByName(book)

            when {
                // 2a. KJV ships complete & offline.
                translation == Translation.KJV -> {
                    val order = bookEntity?.order
                    if (order != null) {
                        val offline = kjvSource.getText(order, chapter, verse)
                        if (!offline.isNullOrBlank()) {
                            return@withContext success(
                                book,
                                chapter,
                                verse,
                                reference,
                                offline,
                                translation
                            )
                        }
                    }
                    fetchFromBibleApi(book, chapter, verse, reference, translation)
                }

                // 2b. Other public-domain translations via the free, key-less API.
                else -> fetchFromBibleApi(book, chapter, verse, reference, translation)
            }
        }

    private suspend fun fetchFromBibleApi(
        book: String,
        chapter: Int,
        verse: Int,
        reference: String,
        translation: Translation,
    ): AppResult<Verse> =
        try {
            val response = bibleApi.getVerse(reference, translation.id)
            val text =
                response.text.trim().ifEmpty {
                    response.verses.joinToString(" ") { it.text.trim() }
                }
            if (text.isBlank()) error("Empty verse response")
            success(book, chapter, verse, reference, text, translation)
        } catch (e: Exception) {
            // Network failure: fall back to the WEB-seeded copy if we have it.
            val seeded = dao.getCachedVerse(book, chapter, verse, Translation.WEB.id)
            if (seeded != null) {
                AppResult.Success(seeded.toVerse(Translation.WEB), fromCache = true)
            } else {
                AppResult.Failure("offline", e)
            }
        }

    private suspend fun success(
        book: String,
        chapter: Int,
        verse: Int,
        reference: String,
        text: String,
        translation: Translation,
    ): AppResult<Verse> {
        dao.cacheVerse(
            VerseEntity(
                book = book,
                chapter = chapter,
                verse = verse,
                translation = translation.id,
                reference = reference,
                text = text,
            )
        )
        return AppResult.Success(
            Verse(book, chapter, verse, reference, text, translation.id),
            fromCache = false,
        )
    }

    override suspend fun getThemes(): List<String> =
        withContext(ioDispatcher) { dao.getThemeNames() }

    override suspend fun getRandomVerseForTheme(
        theme: String,
        translation: Translation,
    ): AppResult<Verse> =
        withContext(ioDispatcher) {
            val candidates = dao.getVersesForTheme(theme)
            if (candidates.isEmpty()) {
                return@withContext AppResult.Failure("No verses for theme")
            }
            val pick = candidates.random()
            getVerse(pick.book, pick.chapter, pick.verse, translation)
        }
}

private fun BookEntity.toDomain(): BibleBook =
    BibleBook(
        order = order,
        name = name,
        abbrev = abbrev,
        testament = if (testament == "NT") Testament.NT else Testament.OT,
        verseCounts = verseCountsCsv.split(",").mapNotNull { it.trim().toIntOrNull() },
    )

private fun VerseEntity.toVerse(translation: Translation): Verse =
    Verse(
        book = book,
        chapter = chapter,
        verse = verse,
        reference = reference,
        text = text,
        translation = translation.id,
    )
