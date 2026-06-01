package com.divinecanvas.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divinecanvas.core.AppResult
import com.divinecanvas.data.prefs.UserPreferences
import com.divinecanvas.domain.model.BannerPosition
import com.divinecanvas.domain.model.BibleBook
import com.divinecanvas.domain.model.CanvasBackground
import com.divinecanvas.domain.model.CanvasFont
import com.divinecanvas.domain.model.Translation
import com.divinecanvas.domain.repository.BackgroundRepository
import com.divinecanvas.domain.repository.BibleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val bibleRepository: BibleRepository,
    private val backgroundRepository: BackgroundRepository,
    private val userPreferences: UserPreferences,
    private val json: Json,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditorUiState(photoSearchAvailable = backgroundRepository.photoSearchAvailable)
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    @OptIn(FlowPreview::class)
    private fun loadInitialData() {
        viewModelScope.launch {
            val books = bibleRepository.getBooks()
            val themes = bibleRepository.getThemes()
            var restored = _uiState.value.copy(books = books, themes = themes)

            // Restore the last session's verse + styling, if any.
            userPreferences.editorSnapshotJson.first()?.let { saved ->
                runCatching { json.decodeFromString(EditorSnapshot.serializer(), saved) }
                    .getOrNull()
                    ?.let { snapshot -> restored = snapshot.restoreInto(restored, books) }
            }
            _uiState.value = restored

            // Persist subsequent changes (debounced; skip the restore emission).
            _uiState
                .map { it.toSnapshot() }
                .distinctUntilChanged()
                .drop(1)
                .debounce(500)
                .collect { snapshot ->
                    userPreferences.saveEditorSnapshot(
                        json.encodeToString(EditorSnapshot.serializer(), snapshot)
                    )
                }
        }
    }

    // --- Mode ---
    fun onModeChange(mode: SelectionMode) = _uiState.update { it.copy(mode = mode) }

    // --- Manual selection (reactive cascade) ---
    fun onBookSelected(book: BibleBook) = _uiState.update {
        it.copy(selectedBook = book, selectedChapter = null, selectedVerse = null)
    }

    fun onChapterSelected(chapter: Int) = _uiState.update {
        it.copy(selectedChapter = chapter, selectedVerse = null)
    }

    fun onVerseSelected(verse: Int) = _uiState.update { it.copy(selectedVerse = verse) }

    fun onTranslationSelected(translation: Translation) =
        _uiState.update { it.copy(translation = translation) }

    // --- Theme selection ---
    fun onThemeSelected(theme: String) = _uiState.update { it.copy(selectedTheme = theme) }

    fun onRandomThemeVerse() {
        val theme = _uiState.value.selectedTheme ?: _uiState.value.themes.randomOrNull() ?: return
        _uiState.update { it.copy(selectedTheme = theme, isLoadingVerse = true) }
        viewModelScope.launch {
            val result = bibleRepository.getRandomVerseForTheme(theme, _uiState.value.translation)
            applyVerseResult(result)
        }
    }

    // --- Load verse ---
    fun onLoadVerse() {
        val state = _uiState.value
        when (state.mode) {
            SelectionMode.THEME -> onRandomThemeVerse()
            SelectionMode.MANUAL -> {
                val book = state.selectedBook ?: return
                val chapter = state.selectedChapter ?: return
                val verse = state.selectedVerse ?: return
                _uiState.update { it.copy(isLoadingVerse = true) }
                viewModelScope.launch {
                    val result = bibleRepository.getVerse(book.name, chapter, verse, state.translation)
                    applyVerseResult(result)
                }
            }
        }
    }

    private fun applyVerseResult(result: AppResult<com.divinecanvas.domain.model.Verse>) {
        when (result) {
            is AppResult.Success -> _uiState.update {
                it.copy(
                    isLoadingVerse = false,
                    canvas = it.canvas.copy(verse = result.data),
                    userMessage = if (result.fromCache) "Showing the offline copy" else null,
                )
            }
            is AppResult.Failure -> _uiState.update {
                it.copy(
                    isLoadingVerse = false,
                    userMessage = when (result.message) {
                        "licensed_no_key" ->
                            "NIV, NKJV & ESV are copyrighted — add your own scripture.api.bible key " +
                                "(API_BIBLE_KEY in local.properties) to use them."
                        "licensed_no_id" ->
                            "No Bible ID is configured for this licensed translation."
                        else -> "Couldn't load the verse. Check your connection."
                    },
                )
            }
        }
    }

    // --- Background ---
    fun onSelectGradient(gradient: CanvasBackground.Gradient) =
        _uiState.update { it.copy(canvas = it.canvas.copy(background = gradient)) }

    fun onSelectPhoto(photo: CanvasBackground.Photo) =
        _uiState.update { it.copy(canvas = it.canvas.copy(background = photo)) }

    fun onPhotoQueryChange(query: String) = _uiState.update { it.copy(photoQuery = query) }

    fun onSearchPhotos() {
        val query = _uiState.value.photoQuery.trim()
        if (query.isEmpty() || !_uiState.value.photoSearchAvailable) return
        _uiState.update { it.copy(isSearchingPhotos = true) }
        viewModelScope.launch {
            when (val result = backgroundRepository.searchPhotos(query)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSearchingPhotos = false, photoResults = result.data)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        isSearchingPhotos = false,
                        userMessage = "Photo search failed — using gradients.",
                    )
                }
            }
        }
    }

    // --- Typography ---
    fun onFontChange(font: CanvasFont) =
        _uiState.update { it.copy(canvas = it.canvas.copy(font = font)) }

    fun onFontSizeChange(size: Float) =
        _uiState.update { it.copy(canvas = it.canvas.copy(fontSizeSp = size)) }

    fun onAlignChange(align: TextAlign) =
        _uiState.update { it.copy(canvas = it.canvas.copy(textAlign = align)) }

    fun onTextColorChange(color: Color) =
        _uiState.update { it.copy(canvas = it.canvas.copy(textColor = color)) }

    fun onToggleShadow(enabled: Boolean) =
        _uiState.update { it.copy(canvas = it.canvas.copy(showShadow = enabled)) }

    fun onOverlayChange(opacity: Float) =
        _uiState.update { it.copy(canvas = it.canvas.copy(overlayOpacity = opacity)) }

    // --- Banner ---
    fun onBannerTextChange(text: String) =
        _uiState.update { it.copy(canvas = it.canvas.copy(banner = it.canvas.banner.copy(text = text))) }

    fun onBannerPositionChange(position: BannerPosition) =
        _uiState.update { it.copy(canvas = it.canvas.copy(banner = it.canvas.banner.copy(position = position))) }

    fun onBannerColorChange(color: Color) =
        _uiState.update { it.copy(canvas = it.canvas.copy(banner = it.canvas.banner.copy(color = color))) }

    fun onMessageShown() = _uiState.update { it.copy(userMessage = null) }
}
