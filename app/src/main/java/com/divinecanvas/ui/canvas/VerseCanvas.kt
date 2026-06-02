package com.divinecanvas.ui.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.divinecanvas.domain.model.BannerPosition
import com.divinecanvas.domain.model.CanvasBackground
import com.divinecanvas.domain.model.CanvasState
import com.divinecanvas.ui.theme.toFontFamily

/**
 * Renders the verse image exactly as it will be exported. Used both for the live preview and
 * (captured via a graphics layer) for the saved/shared bitmap, so what the user sees is precisely
 * what they get.
 */
@Composable
fun VerseCanvas(
    state: CanvasState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(Color.Black)) {
        // --- Background ---
        when (val bg = state.background) {
            is CanvasBackground.Gradient ->
                Box(
                    Modifier.fillMaxSize()
                        .background(Brush.linearGradient(listOf(bg.startColor, bg.endColor)))
                )
            is CanvasBackground.Photo ->
                AsyncImage(
                    model = bg.url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
        }

        // --- Readability scrim ---
        if (state.overlayOpacity > 0f) {
            Box(
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = state.overlayOpacity.coerceIn(0f, 1f)))
            )
        }

        // --- Verse text + reference ---
        val verse = state.verse
        val textStyle =
            TextStyle(
                color = state.textColor,
                fontFamily = state.font.toFontFamily(),
                fontSize = state.fontSizeSp.sp,
                lineHeight = (state.fontSizeSp * 1.35f).sp,
                textAlign = state.textAlign,
                shadow =
                    if (state.showShadow) {
                        Shadow(color = Color.Black.copy(alpha = 0.6f), blurRadius = 8f)
                    } else null,
            )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment =
                when (state.textAlign) {
                    TextAlign.Left -> Alignment.Start
                    TextAlign.Right -> Alignment.End
                    else -> Alignment.CenterHorizontally
                },
        ) {
            Text(
                text = verse?.text ?: "“Choose a verse to begin.”",
                style = textStyle,
            )
            if (verse != null) {
                Text(
                    text = "— ${verse.reference} (${verse.translation.uppercase()})",
                    style =
                        textStyle.copy(
                            fontSize = (state.fontSizeSp * 0.55f).sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    modifier = Modifier.padding(top = 20.dp),
                )
            }
        }

        // --- Optional signature banner (rendered only when text is non-blank) ---
        BannerOverlay(state)
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.BannerOverlay(state: CanvasState) {
    val banner = state.banner
    if (!banner.enabled) return // 0dp, invisible, not rendered.

    val bannerStyle =
        TextStyle(
            color = banner.color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), blurRadius = 6f),
        )

    when (banner.position) {
        BannerPosition.BOTTOM ->
            Text(
                banner.text,
                style = bannerStyle,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
            )
        BannerPosition.TOP ->
            Text(
                banner.text,
                style = bannerStyle,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp),
            )
        BannerPosition.LEFT ->
            Text(
                banner.text,
                style = bannerStyle,
                modifier =
                    Modifier.align(Alignment.CenterStart)
                        .wrapContentSize()
                        .rotate(-90f)
                        .padding(bottom = 20.dp),
            )
        BannerPosition.RIGHT ->
            Text(
                banner.text,
                style = bannerStyle,
                modifier =
                    Modifier.align(Alignment.CenterEnd)
                        .wrapContentSize()
                        .rotate(90f)
                        .padding(bottom = 20.dp),
            )
    }
}
