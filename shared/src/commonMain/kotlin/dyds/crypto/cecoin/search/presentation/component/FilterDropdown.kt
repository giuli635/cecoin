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
import dyds.crypto.cecoin.search.presentation.SearchStrings
import dyds.crypto.cecoin.search.presentation.FilterMode

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
                    FilterMode.ALL -> SearchStrings.ALL_COINS
                    FilterMode.FAVORITES -> SearchStrings.FAVORITES
                },
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(SearchStrings.ALL_COINS) },
                onClick = {
                    onModeSelected(FilterMode.ALL)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(SearchStrings.FAVORITES) },
                onClick = {
                    onModeSelected(FilterMode.FAVORITES)
                    expanded = false
                },
            )
        }
    }
}
