package com.divinecanvas.data.local.kjv

import android.content.Context
import com.divinecanvas.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class KjvBook(
    val abbrev: String = "",
    /** chapters[c][v] = text of verse (v+1) in chapter (c+1). */
    val chapters: List<List<String>> = emptyList(),
)

/**
 * Serves the complete, public-domain KJV bundled in assets so KJV works fully
 * offline. The 4.5 MB dataset is parsed once, lazily, and held in memory.
 */
@Singleton
class KjvOfflineSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val mutex = Mutex()
    @Volatile private var books: List<KjvBook>? = null

    /** Books are stored in canonical order, so [bookOrder] (1-based) maps to index. */
    suspend fun getText(bookOrder: Int, chapter: Int, verse: Int): String? =
        withContext(ioDispatcher) {
            val loaded = ensureLoaded()
            val raw = loaded.getOrNull(bookOrder - 1)
                ?.chapters?.getOrNull(chapter - 1)
                ?.getOrNull(verse - 1)
                ?: return@withContext null
            clean(raw)
        }

    private suspend fun ensureLoaded(): List<KjvBook> {
        books?.let { return it }
        return mutex.withLock {
            books ?: parse().also { books = it }
        }
    }

    private fun parse(): List<KjvBook> {
        val raw = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        return json.decodeFromString(ListSerializer(KjvBook.serializer()), raw)
    }

    /** Remove KJV editorial markers like {italics} and {…: translator notes}. */
    private fun clean(text: String): String =
        text.replace(BRACES, "").replace(WHITESPACE, " ").trim()

    private companion object {
        const val ASSET = "bible/kjv.json"
        val BRACES = Regex("\\{[^}]*}")
        val WHITESPACE = Regex("\\s+")
    }
}
