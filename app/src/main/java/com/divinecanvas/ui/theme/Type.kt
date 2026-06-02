package com.divinecanvas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.divinecanvas.R
import com.divinecanvas.domain.model.CanvasFont

val DivineCanvasTypography = Typography()

/** Real Playfair Display (SIL OFL, bundled in res/font). */
val PlayfairDisplay = FontFamily(Font(R.font.playfair_display))

/** Maps a [CanvasFont] to a real [FontFamily]. */
fun CanvasFont.toFontFamily(): FontFamily =
    when (this) {
        CanvasFont.SANS -> FontFamily.SansSerif
        CanvasFont.SERIF -> FontFamily.Serif
        CanvasFont.PLAYFAIR -> PlayfairDisplay
        CanvasFont.MONO -> FontFamily.Monospace
    }
