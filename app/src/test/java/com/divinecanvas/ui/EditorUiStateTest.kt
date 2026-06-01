package com.divinecanvas.ui

import com.divinecanvas.domain.model.BibleBook
import com.divinecanvas.domain.model.Testament
import com.divinecanvas.ui.editor.EditorUiState
import com.divinecanvas.ui.editor.SelectionMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorUiStateTest {

    private val genesis = BibleBook(1, "Genesis", "GEN", Testament.OT, listOf(31, 25, 24))

    @Test
    fun `chapters derive from selected book`() {
        val state = EditorUiState(selectedBook = genesis)
        assertEquals(listOf(1, 2, 3), state.chapters)
    }

    @Test
    fun `verses derive from selected book and chapter`() {
        val state = EditorUiState(selectedBook = genesis, selectedChapter = 1)
        assertEquals((1..31).toList(), state.verses)
    }

    @Test
    fun `verses empty until chapter chosen`() {
        val state = EditorUiState(selectedBook = genesis)
        assertTrue(state.verses.isEmpty())
    }

    @Test
    fun `manual mode requires full selection to load`() {
        val incomplete = EditorUiState(selectedBook = genesis, selectedChapter = 1)
        assertFalse(incomplete.canLoadVerse)

        val complete = incomplete.copy(selectedVerse = 16)
        assertTrue(complete.canLoadVerse)
    }

    @Test
    fun `theme mode only needs a theme to load`() {
        val state = EditorUiState(mode = SelectionMode.THEME, selectedTheme = "Faith")
        assertTrue(state.canLoadVerse)
    }
}
