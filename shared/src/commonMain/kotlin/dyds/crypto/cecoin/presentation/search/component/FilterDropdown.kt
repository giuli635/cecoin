package dyds.crypto.cecoin.presentation.search.component

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
import dyds.crypto.cecoin.presentation.search.FilterMode

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
                    FilterMode.ALL -> "All Coins"
                    FilterMode.FAVORITES -> "Favorites"
                },
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All Coins") },
                onClick = {
                    onModeSelected(FilterMode.ALL)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Favorites") },
                onClick = {
                    onModeSelected(FilterMode.FAVORITES)
                    expanded = false
                },
            )
        }
    }
}
