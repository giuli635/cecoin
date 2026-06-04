package dyds.crypto.cecoin.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            label = { Text("Search symbol (e.g., BTC, ETH)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = asyncFilteredCoins !is Loadable.Loading,
        )

        Text(
            text = "Available Coins",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )

        buildAsyncComposable(
            viewModel::onCancelLoadSymbols,
            viewModel::retryLoadSymbols,
            coinsListRenderer(uiState.searchQuery, viewModel, onCoinSelected),
        )(asyncFilteredCoins, Modifier.fillMaxSize())
    }
}

private fun coinsListRenderer(
    searchQuery: String,
    viewModel: CoinSearchViewModel,
    onCoinSelected: (String) -> Unit
): Renderer<List<String>> = { coins, modifier ->
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(coins) { coin ->
            CoinItem(
                coin = coin,
                onClick = {
                    viewModel.selectCoin(coin)
                    onCoinSelected(coin)
                },
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = coin,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}