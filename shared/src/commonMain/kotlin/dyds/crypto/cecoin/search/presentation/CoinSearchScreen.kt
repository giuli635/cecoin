package dyds.crypto.cecoin.search.presentation

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.presentation.Renderer
import dyds.crypto.cecoin.search.presentation.component.CoinItem
import cecoin.shared.generated.resources.Res
import cecoin.shared.generated.resources.search_available_coins
import cecoin.shared.generated.resources.search_field_label
import cecoin.shared.generated.resources.search_no_coins_available
import cecoin.shared.generated.resources.search_no_coins_found_prefix
import cecoin.shared.generated.resources.search_screen_title
import dyds.crypto.cecoin.search.presentation.component.FilterDropdown
import dyds.crypto.cecoin.core.presentation.utils.buildAsyncComposable
import dyds.crypto.cecoin.core.domain.state.Loadable
import dyds.crypto.cecoin.core.presentation.utils.resolve
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

private const val TOGGLE_ERROR_CLEAR_DELAY_MS = 4000L

@Composable
fun CoinSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: CoinSearchViewModel,
    onCoinSelected: (CryptoSymbol) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val asyncFilteredCoins by viewModel.filteredCoins.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val toggleError by viewModel.toggleError.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSymbols()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.search_screen_title),
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
                label = { Text(stringResource(Res.string.search_field_label)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                enabled = asyncFilteredCoins !is Loadable.Loading,
            )

            FilterDropdown(
                currentMode = uiState.filterMode,
                onModeSelected = viewModel::setFilterMode,
            )
        }

        toggleError?.let { error ->
            LaunchedEffect(toggleError) {
                delay(TOGGLE_ERROR_CLEAR_DELAY_MS.milliseconds)
                viewModel.clearToggleError()
            }
            Text(
                text = error.uiText.resolve(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Text(
            text = stringResource(Res.string.search_available_coins),
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
    onCoinSelected: (CryptoSymbol) -> Unit,
    favorites: Set<CryptoSymbol>,
): Renderer<List<CryptoSymbol>> = { coins, modifier ->
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
                    text = "${stringResource(Res.string.search_no_coins_found_prefix)}$searchQuery'",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else if (coins.isEmpty()) {
            item {
                Text(
                    text = stringResource(Res.string.search_no_coins_available),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
