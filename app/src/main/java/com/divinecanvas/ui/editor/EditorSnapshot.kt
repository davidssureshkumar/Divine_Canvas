package com.divinecanvas.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import com.divinecanvas.domain.model.BannerConfig
import com.divinecanvas.domain.model.BannerPosition
import com.divinecanvas.domain.model.BibleBook
import com.divinecanvas.domain.model.CanvasBackground
import com.divinecanvas.domain.model.CanvasFont
import com.divinecanvas.domain.model.CanvasState
import com.divinecanvas.domain.model.DefaultBackgrounds
import com.divinecanvas.domain.model.Translation
import com.divinecanvas.domain.model.Verse
import kotlinx.serialization.Serializable

/**
 * Flat, serializable mirror of the editor state so the user's last verse and full styling are
 * restored on the next launch. Persisted as JSON via DataStore.
 */
@Serializable
data class EditorSnapshot(
    val mode: String = SelectionMode.MANUAL.name,
    val bookName: String? = null,
    val chapter: Int? = null,
    val verse: Int? = null,
    val translationId: String = Translation.WEB.id,
    val theme: String? = null,

    // Last loaded verse (so it reappears without a network call).
    val loadedBook: String? = null,
    val loadedChapter: Int? = null,
    val loadedVerse: Int? = null,
    val loadedReference: String? = null,
    val loadedText: String? = null,
    val loadedTranslationId: String? = null,

    // Style.
    val backgroundKind: String = "gradient",
    val gradientId: String = DefaultBackgrounds.first().id,
    val photoId: String? = null,
    val photoUrl: String? = null,
    val photoThumb: String? = null,
    val photoAttr: String? = null,
    val fontName: String = CanvasFont.SERIF.name,
    val fontSize: Float = 30f,
    val align: String = "Center",
    val textColorArgb: Int = Color.White.toArgb(),
    val showShadow: Boolean = true,
    val overlay: Float = 0.35f,
    val bannerText: String = "",
    val bannerPosition: String = BannerPosition.BOTTOM.name,
    val bannerColorArgb: Int = Color.White.toArgb(),
    val lineHeightMultiplier: Float = 1.35f,
    val letterSpacingEm: Float = 0f,
)

fun EditorUiState.toSnapshot(): EditorSnapshot {
    val c = canvas
    val bg = c.background
    return EditorSnapshot(
        mode = mode.name,
        bookName = selectedBook?.name,
        chapter = selectedChapter,
        verse = selectedVerse,
        translationId = translation.id,
        theme = selectedTheme,
        loadedBook = c.verse?.book,
        loadedChapter = c.verse?.chapter,
        loadedVerse = c.verse?.verse,
        loadedReference = c.verse?.reference,
        loadedText = c.verse?.text,
        loadedTranslationId = c.verse?.translation,
        backgroundKind = if (bg is CanvasBackground.Photo) "photo" else "gradient",
        gradientId = (bg as? CanvasBackground.Gradient)?.id ?: DefaultBackgrounds.first().id,
        photoId = (bg as? CanvasBackground.Photo)?.id,
        photoUrl = (bg as? CanvasBackground.Photo)?.url,
        photoThumb = (bg as? CanvasBackground.Photo)?.thumbnailUrl,
        photoAttr = (bg as? CanvasBackground.Photo)?.attribution,
        fontName = c.font.name,
        fontSize = c.fontSizeSp,
        align = c.textAlign.toName(),
        textColorArgb = c.textColor.toArgb(),
        showShadow = c.showShadow,
        overlay = c.overlayOpacity,
        bannerText = c.banner.text,
        bannerPosition = c.banner.position.name,
        bannerColorArgb = c.banner.color.toArgb(),
        lineHeightMultiplier = c.lineHeightMultiplier,
        letterSpacingEm = c.letterSpacingEm,
    )
}

/** Rebuild a restored UI state on top of [base] (which already holds books/themes). */
fun EditorSnapshot.restoreInto(base: EditorUiState, books: List<BibleBook>): EditorUiState {
    val book = bookName?.let { name -> books.firstOrNull { it.name == name } }
    val restoredVerse =
        if (loadedText != null && loadedBook != null) {
            Verse(
                book = loadedBook,
                chapter = loadedChapter ?: 0,
                verse = loadedVerse ?: 0,
                reference = loadedReference ?: loadedBook,
                text = loadedText,
                translation = loadedTranslationId ?: translationId,
            )
        } else null

    val background: CanvasBackground =
        if (backgroundKind == "photo" && !photoUrl.isNullOrBlank()) {
            CanvasBackground.Photo(
                id = photoId ?: photoUrl,
                url = photoUrl,
                thumbnailUrl = photoThumb ?: photoUrl,
                attribution = photoAttr.orEmpty(),
            )
        } else {
            DefaultBackgrounds.byId(gradientId)
        }

    return base.copy(
        mode = runCatching { SelectionMode.valueOf(mode) }.getOrDefault(SelectionMode.MANUAL),
        selectedBook = book,
        selectedChapter = chapter,
        selectedVerse = verse,
        translation = Translation.fromId(translationId),
        selectedTheme = theme,
        canvas =
            CanvasState(
                verse = restoredVerse,
                background = background,
                font = runCatching { CanvasFont.valueOf(fontName) }.getOrDefault(CanvasFont.SERIF),
                fontSizeSp = fontSize,
                textAlign = align.toTextAlign(),
                textColor = Color(textColorArgb),
                showShadow = showShadow,
                overlayOpacity = overlay,
                banner =
                    BannerConfig(
                        text = bannerText,
                        position =
                            runCatching { BannerPosition.valueOf(bannerPosition) }
                                .getOrDefault(BannerPosition.BOTTOM),
                        color = Color(bannerColorArgb),
                    ),
                lineHeightMultiplier = lineHeightMultiplier,
                letterSpacingEm = letterSpacingEm,
            ),
    )
}

private fun TextAlign.toName(): String =
    when (this) {
        TextAlign.Left -> "Left"
        TextAlign.Right -> "Right"
        else -> "Center"
    }

private fun String.toTextAlign(): TextAlign =
    when (this) {
        "Left" -> TextAlign.Left
        "Right" -> TextAlign.Right
        else -> TextAlign.Center
    }
