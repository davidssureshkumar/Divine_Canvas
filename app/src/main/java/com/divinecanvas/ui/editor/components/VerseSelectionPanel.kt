package com.divinecanvas.ui.editor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.divinecanvas.R
import com.divinecanvas.domain.model.BibleBook
import com.divinecanvas.domain.model.Testament
import com.divinecanvas.domain.model.Translation
import com.divinecanvas.ui.editor.EditorUiState
import com.divinecanvas.ui.editor.SelectionMode

@Composable
fun VerseSelectionPanel(
    state: EditorUiState,
    onModeChange: (SelectionMode) -> Unit,
    onBookSelected: (BibleBook) -> Unit,
    onChapterSelected: (Int) -> Unit,
    onVerseSelected: (Int) -> Unit,
    onTranslationSelected: (Translation) -> Unit,
    onThemeSelected: (String) -> Unit,
    onRandomTheme: () -> Unit,
    onLoadVerse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Mode toggle
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = state.mode == SelectionMode.MANUAL,
                onClick = { onModeChange(SelectionMode.MANUAL) },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
            ) {
                Text(stringResource(R.string.mode_manual))
            }
            SegmentedButton(
                selected = state.mode == SelectionMode.THEME,
                onClick = { onModeChange(SelectionMode.THEME) },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
            ) {
                Text(stringResource(R.string.mode_theme))
            }
        }

        when (state.mode) {
            SelectionMode.MANUAL ->
                ManualSelection(
                    state = state,
                    onBookSelected = onBookSelected,
                    onChapterSelected = onChapterSelected,
                    onVerseSelected = onVerseSelected,
                    onTranslationSelected = onTranslationSelected,
                )
            SelectionMode.THEME ->
                ThemeSelection(
                    state = state,
                    onThemeSelected = onThemeSelected,
                    onRandomTheme = onRandomTheme,
                )
        }

        Button(
            onClick = onLoadVerse,
            enabled = state.canLoadVerse && !state.isLoadingVerse,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.mode == SelectionMode.THEME) {
                    stringResource(R.string.action_random_theme)
                } else {
                    stringResource(R.string.action_load_verse)
                }
            )
        }
    }
}

@Composable
private fun ManualSelection(
    state: EditorUiState,
    onBookSelected: (BibleBook) -> Unit,
    onChapterSelected: (Int) -> Unit,
    onVerseSelected: (Int) -> Unit,
    onTranslationSelected: (Translation) -> Unit,
) {
    val otHeader = stringResource(R.string.group_old_testament)
    val ntHeader = stringResource(R.string.group_new_testament)

    val bookOptions =
        remember(state.books) {
            var lastTestament: Testament? = null
            state.books.map { book ->
                val header =
                    if (book.testament != lastTestament) {
                        if (book.testament == Testament.OT) otHeader else ntHeader
                    } else null
                lastTestament = book.testament
                DropdownOption(value = book, label = book.name, sectionHeader = header)
            }
        }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LabeledDropdown(
            label = stringResource(R.string.label_book),
            placeholder = stringResource(R.string.hint_select_book),
            selectedLabel = state.selectedBook?.name,
            options = bookOptions,
            onSelected = onBookSelected,
        )
        LabeledDropdown(
            label = stringResource(R.string.label_chapter),
            placeholder = stringResource(R.string.hint_select_chapter),
            selectedLabel = state.selectedChapter?.toString(),
            options = state.chapters.map { DropdownOption(it, it.toString()) },
            onSelected = onChapterSelected,
            enabled = state.selectedBook != null,
        )
        LabeledDropdown(
            label = stringResource(R.string.label_verse),
            placeholder = stringResource(R.string.hint_select_verse),
            selectedLabel = state.selectedVerse?.toString(),
            options = state.verses.map { DropdownOption(it, it.toString()) },
            onSelected = onVerseSelected,
            enabled = state.selectedChapter != null,
        )
        LabeledDropdown(
            label = stringResource(R.string.label_translation),
            placeholder = "",
            selectedLabel = state.translation.displayName,
            options = Translation.entries.map { DropdownOption(it, it.displayName) },
            onSelected = onTranslationSelected,
        )
    }
}

@Composable
private fun ThemeSelection(
    state: EditorUiState,
    onThemeSelected: (String) -> Unit,
    onRandomTheme: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.themes.forEach { theme ->
                FilterChip(
                    selected = state.selectedTheme == theme,
                    onClick = { onThemeSelected(theme) },
                    label = { Text(theme) },
                    leadingIcon =
                        if (state.selectedTheme == theme) {
                            { Icon(Icons.Filled.AutoAwesome, contentDescription = null) }
                        } else null,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}
