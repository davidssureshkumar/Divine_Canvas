package com.divinecanvas.data

import com.divinecanvas.data.local.seed.VersificationFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the offline Bible data: every per-chapter verse count in
 * versification.json (which drives the dropdowns) must match the actual number
 * of verses in the bundled KJV text (kjv.json). If either asset drifts, the
 * dropdowns would offer verses that don't exist offline (or hide ones that do),
 * so this test fails the build instead.
 *
 * Unit tests run with the module directory as the working directory.
 */
class KjvVersificationIntegrityTest {

    @Serializable
    private data class KjvBookT(
        val abbrev: String = "",
        val chapters: List<List<String>> = emptyList(),
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val assets = File("src/main/assets/bible")

    @Test
    fun `versification matches bundled KJV exactly`() {
        val versFile = File(assets, "versification.json")
        val kjvFile = File(assets, "kjv.json")
        assertTrue("versification.json missing", versFile.exists())
        assertTrue("kjv.json missing", kjvFile.exists())

        val versification = json.decodeFromString(VersificationFile.serializer(), versFile.readText())
        val kjv = json.decodeFromString(ListSerializer(KjvBookT.serializer()), kjvFile.readText())

        assertEquals("Both sources must list all 66 books", 66, versification.books.size)
        assertEquals("KJV must contain all 66 books", 66, kjv.size)

        versification.books.sortedBy { it.order }.forEachIndexed { index, book ->
            val kjvBook = kjv[index]
            assertEquals(
                "Chapter count mismatch in ${book.name}",
                book.verses.size,
                kjvBook.chapters.size,
            )
            book.verses.forEachIndexed { c, expectedVerseCount ->
                assertEquals(
                    "Verse count mismatch in ${book.name} ${c + 1}",
                    expectedVerseCount,
                    kjvBook.chapters[c].size,
                )
            }
        }
    }
}
