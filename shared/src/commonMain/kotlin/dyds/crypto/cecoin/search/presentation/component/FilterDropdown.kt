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
import dyds.crypto.cecoin.search.presentation.FilterMode

private const val ALL_COINS_LABEL = "Todas"
private const val FAVORITES_LABEL = "Favoritas"

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
                    FilterMode.ALL -> ALL_COINS_LABEL
                    FilterMode.FAVORITES -> FAVORITES_LABEL
                },
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(ALL_COINS_LABEL) },
                onClick = {
                    onModeSelected(FilterMode.ALL)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(FAVORITES_LABEL) },
                onClick = {
                    onModeSelected(FilterMode.FAVORITES)
                    expanded = false
                },
            )
        }
    }
}
