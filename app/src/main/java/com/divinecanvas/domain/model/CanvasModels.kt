package com.divinecanvas.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

/** Where the optional signature banner is anchored on the canvas. */
enum class BannerPosition {
    BOTTOM,
    TOP,
    LEFT,
    RIGHT
}

/** Font families offered in the typography panel. Mapped to real fonts in the UI layer. */
enum class CanvasFont(val displayName: String) {
    SANS("Sans"),
    SERIF("Serif"),
    PLAYFAIR("Playfair"),
    MONO("Mono"),
}

/**
 * A background can be a built-in gradient (always available offline) or a remote photo fetched from
 * Unsplash/Pexels.
 */
sealed interface CanvasBackground {
    data class Gradient(
        val id: String,
        val startColor: Color,
        val endColor: Color,
        val angleDegrees: Float = 45f,
    ) : CanvasBackground

    data class Photo(
        val id: String,
        val url: String,
        val thumbnailUrl: String,
        val attribution: String,
    ) : CanvasBackground
}

/** Optional signature/watermark configuration. Blank text => fully disabled. */
data class BannerConfig(
    val text: String = "",
    val position: BannerPosition = BannerPosition.BOTTOM,
    val color: Color = Color.White,
) {
    /** When false, the banner occupies zero space and is not rendered at all. */
    val enabled: Boolean
        get() = text.isNotBlank()
}

/** The complete, render-ready description of the canvas. */
data class CanvasState(
    val verse: Verse? = null,
    val background: CanvasBackground = DefaultBackgrounds.first(),
    val font: CanvasFont = CanvasFont.SERIF,
    val fontSizeSp: Float = 30f,
    val textAlign: TextAlign = TextAlign.Center,
    val textColor: Color = Color.White,
    val showShadow: Boolean = true,
    /** 0f = no overlay, 1f = fully opaque scrim behind the text for readability. */
    val overlayOpacity: Float = 0.35f,
    val banner: BannerConfig = BannerConfig(),
    val lineHeightMultiplier: Float = 1.35f,
    val letterSpacingEm: Float = 0f,
)

/** Built-in gradients that require zero internet connection. */
object DefaultBackgrounds {
    fun all(): List<CanvasBackground.Gradient> =
        listOf(
            CanvasBackground.Gradient("dawn", Color(0xFFFF9966), Color(0xFFFF5E62)),
            CanvasBackground.Gradient("dusk", Color(0xFF7C4DFF), Color(0xFF2A1A5E)),
            CanvasBackground.Gradient("ocean", Color(0xFF2193B0), Color(0xFF6DD5ED)),
            CanvasBackground.Gradient("forest", Color(0xFF134E5E), Color(0xFF71B280)),
            CanvasBackground.Gradient("gold", Color(0xFFF7971E), Color(0xFFFFD200)),
            CanvasBackground.Gradient("rose", Color(0xFFEE9CA7), Color(0xFFFFDDE1)),
            CanvasBackground.Gradient("midnight", Color(0xFF0F2027), Color(0xFF2C5364)),
            CanvasBackground.Gradient("lavender", Color(0xFF8E2DE2), Color(0xFF4A00E0)),
            CanvasBackground.Gradient("sunrise", Color(0xFFFDC830), Color(0xFFF37335)),
            CanvasBackground.Gradient("slate", Color(0xFF232526), Color(0xFF414345)),
        )

    fun first(): CanvasBackground.Gradient = all().first()

    fun byId(id: String): CanvasBackground.Gradient = all().firstOrNull { it.id == id } ?: first()
}
