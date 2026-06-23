package dyds.crypto.cecoin.search.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cecoin.shared.generated.resources.Res
import cecoin.shared.generated.resources.search_filter_all
import cecoin.shared.generated.resources.search_filter_favorites
import dyds.crypto.cecoin.search.presentation.FilterMode
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilterDropdown(
    currentMode: FilterMode,
    onModeSelected: (FilterMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                text = when (currentMode) {
                    FilterMode.ALL -> stringResource(Res.string.search_filter_all)
                    FilterMode.FAVORITES -> stringResource(Res.string.search_filter_favorites)
                },
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.search_filter_all)) },
                onClick = {
                    onModeSelected(FilterMode.ALL)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.search_filter_favorites)) },
                onClick = {
                    onModeSelected(FilterMode.FAVORITES)
                    expanded = false
                },
            )
        }
    }
}
