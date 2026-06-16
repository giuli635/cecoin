package dyds.crypto.cecoin.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.presentation.search.component.CoinItem
import dyds.crypto.cecoin.presentation.search.component.FilterDropdown
import dyds.crypto.cecoin.presentation.utils.buildAsyncComposable
import dyds.crypto.cecoin.utils.Loadable

private const val SEARCH_TITLE = "Buscar Criptos"
private const val SEARCH_LABEL = "Buscar símbolo (ej: BTC, ETH)"
private const val AVAILABLE_COINS_LABEL = "Criptos disponibles"
private const val NO_COINS_FOUND = "No se encontraron criptos con '"

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
            text = SEARCH_TITLE,
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
                label = { Text(SEARCH_LABEL) },
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
            text = AVAILABLE_COINS_LABEL,
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
                    onCoinSelected(coin)
                },
                onFavoriteClick = { viewModel.toggleFavorite(coin) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (coins.isEmpty() && searchQuery.isNotEmpty()) {
            item {
                Text(
                    text = "$NO_COINS_FOUND$searchQuery'",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
