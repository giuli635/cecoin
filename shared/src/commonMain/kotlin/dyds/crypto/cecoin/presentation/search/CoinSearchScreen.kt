package dyds.crypto.cecoin.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.presentation.utils.buildAsyncComposable
import dyds.crypto.cecoin.utils.Loadable

@Composable
fun CoinSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: CoinSearchViewModel,
    onCoinSelected: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val asyncFilteredCoins by viewModel.filteredCoins.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Search Coins",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search symbol (e.g., BTC, ETH)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                enabled = asyncFilteredCoins !is Loadable.Loading,
            )

            FilterDropdown(
                currentMode = uiState.filterMode,
                onModeSelected = viewModel::setFilterMode,
            )
        }

        Text(
            text = "Available Coins",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )

        buildAsyncComposable(
            viewModel::onCancelLoadSymbols,
            viewModel::retryLoadSymbols,
            coinsListRenderer(uiState.searchQuery, viewModel, onCoinSelected, favorites),
        )(asyncFilteredCoins, Modifier.fillMaxSize())
    }
}

@Composable
private fun FilterDropdown(
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

private fun coinsListRenderer(
    searchQuery: String,
    viewModel: CoinSearchViewModel,
    onCoinSelected: (String) -> Unit,
    favorites: Set<String>,
): Renderer<List<String>> = { coins, modifier ->
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(coins) { coin ->
            CoinItem(
                coin = coin,
                isFavorite = coin in favorites,
                onClick = {
                    viewModel.selectCoin(coin)
                    onCoinSelected(coin)
                },
                onFavoriteClick = { viewModel.toggleFavorite(coin) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (coins.isEmpty() && searchQuery.isNotEmpty()) {
            item {
                Text(
                    text = "No coins found matching '$searchQuery'",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
fun CoinItem(
    coin: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = coin,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = if (isFavorite) "\u2605" else "\u2606",
            style = MaterialTheme.typography.titleMedium,
            color = if (isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clickable { onFavoriteClick() }
                .padding(start = 8.dp),
        )
    }
}
