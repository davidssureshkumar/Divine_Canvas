package com.divinecanvas.ui.editor

import com.divinecanvas.domain.model.BibleBook
import com.divinecanvas.domain.model.CanvasBackground
import com.divinecanvas.domain.model.CanvasState
import com.divinecanvas.domain.model.Translation

enum class SelectionMode { MANUAL, THEME }

data class EditorUiState(
    val mode: SelectionMode = SelectionMode.MANUAL,
    val books: List<BibleBook> = emptyList(),
    val selectedBook: BibleBook? = null,
    val selectedChapter: Int? = null,
    val selectedVerse: Int? = null,
    val translation: Translation = Translation.WEB,

    val themes: List<String> = emptyList(),
    val selectedTheme: String? = null,

    val canvas: CanvasState = CanvasState(),

    // Background photo search
    val photoSearchAvailable: Boolean = false,
    val photoQuery: String = "",
    val photoResults: List<CanvasBackground.Photo> = emptyList(),
    val isSearchingPhotos: Boolean = false,

    val isLoadingVerse: Boolean = false,
    val userMessage: String? = null,
) {
    val chapters: List<Int>
        get() = selectedBook?.let { (1..it.chapterCount).toList() } ?: emptyList()

    val verses: List<Int>
        get() {
            val book = selectedBook ?: return emptyList()
            val chapter = selectedChapter ?: return emptyList()
            return (1..book.versesInChapter(chapter)).toList()
        }

    val canLoadVerse: Boolean
        get() = when (mode) {
            SelectionMode.MANUAL ->
                selectedBook != null && selectedChapter != null && selectedVerse != null
            SelectionMode.THEME -> selectedTheme != null
        }
}
