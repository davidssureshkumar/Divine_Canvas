package com.divinecanvas.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.divinecanvas.domain.model.BannerPosition
import com.divinecanvas.domain.model.BibleBook
import com.divinecanvas.domain.model.CanvasFont
import com.divinecanvas.domain.model.CanvasState
import com.divinecanvas.domain.model.Testament
import com.divinecanvas.domain.model.Translation
import com.divinecanvas.domain.model.TranslationTier
import com.divinecanvas.domain.model.Verse
import com.divinecanvas.ui.editor.EditorUiState
import com.divinecanvas.ui.editor.restoreInto
import com.divinecanvas.ui.editor.toSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorSnapshotTest {

    private val john = BibleBook(43, "John", "JHN", Testament.NT, List(21) { 30 })

    @Test
    fun `KJV is public domain and bundled offline`() {
        assertEquals(TranslationTier.PUBLIC_DOMAIN, Translation.KJV.tier)
        assertTrue(Translation.KJV.bundledOffline)
        assertFalse(Translation.KJV.isLicensed)
    }

    @Test
    fun `NIV NKJV ESV are licensed and not bundled`() {
        listOf(Translation.NIV, Translation.NKJV, Translation.ESV).forEach {
            assertTrue("${it.id} should be licensed", it.isLicensed)
            assertFalse("${it.id} must not be bundled", it.bundledOffline)
        }
    }

    @Test
    fun `snapshot round-trips selection, verse and styling`() {
        val original = EditorUiState(
            selectedBook = john,
            selectedChapter = 3,
            selectedVerse = 16,
            translation = Translation.KJV,
            canvas = CanvasState(
                verse = Verse("John", 3, 16, "John 3:16", "For God so loved…", "kjv"),
                font = CanvasFont.PLAYFAIR,
                fontSizeSp = 42f,
                textAlign = TextAlign.Left,
                textColor = Color(0xFFFFD54F),
                showShadow = false,
                overlayOpacity = 0.5f,
            ),
        )

        val restored = original.toSnapshot().restoreInto(EditorUiState(books = listOf(john)), listOf(john))

        assertEquals(john, restored.selectedBook)
        assertEquals(3, restored.selectedChapter)
        assertEquals(16, restored.selectedVerse)
        assertEquals(Translation.KJV, restored.translation)
        assertEquals("For God so loved…", restored.canvas.verse?.text)
        assertEquals(CanvasFont.PLAYFAIR, restored.canvas.font)
        assertEquals(42f, restored.canvas.fontSizeSp, 0.001f)
        assertEquals(TextAlign.Left, restored.canvas.textAlign)
        assertFalse(restored.canvas.showShadow)
        assertEquals(BannerPosition.BOTTOM, restored.canvas.banner.position)
    }
}
