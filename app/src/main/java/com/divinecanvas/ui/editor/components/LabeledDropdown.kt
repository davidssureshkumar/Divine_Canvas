package com.divinecanvas.ui.editor.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** A single selectable option, optionally preceded by a section header. */
data class DropdownOption<T>(
    val value: T,
    val label: String,
    val sectionHeader: String? = null,
)

/**
 * A reactive Material3 exposed dropdown. Disabled automatically when [options] is empty (e.g.
 * Chapter before a Book is chosen), which keeps the cascade error-proof.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LabeledDropdown(
    label: String,
    placeholder: String,
    selectedLabel: String?,
    options: List<DropdownOption<T>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val isEnabled = enabled && options.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled,
        onExpandedChange = { if (isEnabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedLabel ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = isEnabled,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded && isEnabled,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 360.dp),
        ) {
            options.forEachIndexed { index, option ->
                option.sectionHeader?.let { header ->
                    if (index != 0) HorizontalDivider()
                    Box(Modifier.fillMaxWidth()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    header,
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            onClick = {},
                            enabled = false,
                        )
                    }
                }
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option.value)
                        expanded = false
                    },
                )
            }
        }
    }
}
