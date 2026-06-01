package com.divinecanvas.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.divinecanvas.R
import com.divinecanvas.domain.model.BannerPosition
import com.divinecanvas.domain.model.CanvasBackground
import com.divinecanvas.domain.model.CanvasFont
import com.divinecanvas.ui.editor.EditorUiState

/** Curated text/banner color palette (kept small and clean). */
val TextColorPalette = listOf(
    Color.White,
    Color.Black,
    Color(0xFFFFD54F),
    Color(0xFFFF8A80),
    Color(0xFF80D8FF),
    Color(0xFFB9F6CA),
    Color(0xFFEA80FC),
)

@Composable
fun CustomizationPanel(
    state: EditorUiState,
    onSelectGradient: (CanvasBackground.Gradient) -> Unit,
    onSelectPhoto: (CanvasBackground.Photo) -> Unit,
    onPhotoQueryChange: (String) -> Unit,
    onSearchPhotos: () -> Unit,
    onFontChange: (CanvasFont) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onAlignChange: (TextAlign) -> Unit,
    onTextColorChange: (Color) -> Unit,
    onToggleShadow: (Boolean) -> Unit,
    onOverlayChange: (Float) -> Unit,
    onBannerTextChange: (String) -> Unit,
    onBannerPositionChange: (BannerPosition) -> Unit,
    onBannerColorChange: (Color) -> Unit,
    gradients: List<CanvasBackground.Gradient>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        BackgroundSection(
            state = state,
            gradients = gradients,
            onSelectGradient = onSelectGradient,
            onSelectPhoto = onSelectPhoto,
            onPhotoQueryChange = onPhotoQueryChange,
            onSearchPhotos = onSearchPhotos,
        )
        TypographySection(
            state = state,
            onFontChange = onFontChange,
            onFontSizeChange = onFontSizeChange,
            onAlignChange = onAlignChange,
            onTextColorChange = onTextColorChange,
            onToggleShadow = onToggleShadow,
            onOverlayChange = onOverlayChange,
        )
        BannerSection(
            state = state,
            onBannerTextChange = onBannerTextChange,
            onBannerPositionChange = onBannerPositionChange,
            onBannerColorChange = onBannerColorChange,
        )
    }
}

@Composable
private fun SectionTitle(text: String) =
    Text(text = text, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)

@Composable
private fun BackgroundSection(
    state: EditorUiState,
    gradients: List<CanvasBackground.Gradient>,
    onSelectGradient: (CanvasBackground.Gradient) -> Unit,
    onSelectPhoto: (CanvasBackground.Photo) -> Unit,
    onPhotoQueryChange: (String) -> Unit,
    onSearchPhotos: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.section_background))

        // Gradients (always available offline)
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            gradients.forEach { gradient ->
                val selected = (state.canvas.background as? CanvasBackground.Gradient)?.id == gradient.id
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(gradient.startColor, gradient.endColor)))
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickable { onSelectGradient(gradient) },
                )
            }
        }

        // Photo search
        if (state.photoSearchAvailable) {
            OutlinedTextField(
                value = state.photoQuery,
                onValueChange = onPhotoQueryChange,
                label = { Text(stringResource(R.string.bg_search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                trailingIcon = {
                    IconButton(onClick = onSearchPhotos) {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.isSearchingPhotos) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }
            if (state.photoResults.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.photoResults.forEach { photo ->
                        val selected = (state.canvas.background as? CanvasBackground.Photo)?.id == photo.id
                        coil.compose.AsyncImage(
                            model = photo.thumbnailUrl,
                            contentDescription = photo.attribution,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 48.dp, height = 84.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable { onSelectPhoto(photo) },
                        )
                    }
                }
            }
        } else {
            Text(
                stringResource(R.string.bg_photos_need_key),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun TypographySection(
    state: EditorUiState,
    onFontChange: (CanvasFont) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onAlignChange: (TextAlign) -> Unit,
    onTextColorChange: (Color) -> Unit,
    onToggleShadow: (Boolean) -> Unit,
    onOverlayChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.section_typography))

        // Fonts
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasFont.entries.forEach { font ->
                FilterChip(
                    selected = state.canvas.font == font,
                    onClick = { onFontChange(font) },
                    label = { Text(font.displayName) },
                )
            }
        }

        // Size
        Text(stringResource(R.string.label_font_size) + ": ${state.canvas.fontSizeSp.toInt()}sp")
        Slider(
            value = state.canvas.fontSizeSp,
            onValueChange = onFontSizeChange,
            valueRange = 16f..56f,
        )

        // Alignment
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AlignButton(Icons.AutoMirrored.Filled.FormatAlignLeft, state.canvas.textAlign == TextAlign.Left) {
                onAlignChange(TextAlign.Left)
            }
            AlignButton(Icons.Filled.FormatAlignCenter, state.canvas.textAlign == TextAlign.Center) {
                onAlignChange(TextAlign.Center)
            }
            AlignButton(Icons.AutoMirrored.Filled.FormatAlignRight, state.canvas.textAlign == TextAlign.Right) {
                onAlignChange(TextAlign.Right)
            }
        }

        // Text color
        Text(stringResource(R.string.label_text_color))
        ColorSwatchRow(
            colors = TextColorPalette,
            selected = state.canvas.textColor,
            onSelected = onTextColorChange,
        )

        // Shadow + overlay
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.label_shadow), modifier = Modifier.weight(1f))
            Switch(checked = state.canvas.showShadow, onCheckedChange = onToggleShadow)
        }
        Text(stringResource(R.string.label_overlay) + ": ${(state.canvas.overlayOpacity * 100).toInt()}%")
        Slider(
            value = state.canvas.overlayOpacity,
            onValueChange = onOverlayChange,
            valueRange = 0f..0.8f,
        )
    }
}

@Composable
private fun BannerSection(
    state: EditorUiState,
    onBannerTextChange: (String) -> Unit,
    onBannerPositionChange: (BannerPosition) -> Unit,
    onBannerColorChange: (Color) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.section_banner))
        OutlinedTextField(
            value = state.canvas.banner.text,
            onValueChange = onBannerTextChange,
            label = { Text(stringResource(R.string.banner_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(R.string.banner_empty_note),
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
        )

        if (state.canvas.banner.enabled) {
            Text(stringResource(R.string.banner_position))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BannerPosition.entries.forEach { pos ->
                    FilterChip(
                        selected = state.canvas.banner.position == pos,
                        onClick = { onBannerPositionChange(pos) },
                        label = { Text(positionLabel(pos)) },
                    )
                }
            }
            ColorSwatchRow(
                colors = TextColorPalette,
                selected = state.canvas.banner.color,
                onSelected = onBannerColorChange,
            )
        }
    }
}

@Composable
private fun positionLabel(pos: BannerPosition): String = when (pos) {
    BannerPosition.BOTTOM -> stringResource(R.string.banner_pos_bottom)
    BannerPosition.TOP -> stringResource(R.string.banner_pos_top)
    BannerPosition.LEFT -> stringResource(R.string.banner_pos_left)
    BannerPosition.RIGHT -> stringResource(R.string.banner_pos_right)
}

@Composable
private fun AlignButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (selected) {
        androidx.compose.material3.MaterialTheme.colorScheme.primary
    } else {
        androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    }
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun ColorSwatchRow(
    colors: List<Color>,
    selected: Color,
    onSelected: (Color) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEach { color ->
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = CircleShape,
                    )
                    .clickable { onSelected(color) },
                contentAlignment = Alignment.Center,
            ) {
                if (color == selected) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = stringResource(R.string.cd_color_swatch),
                        tint = if (color == Color.White) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
