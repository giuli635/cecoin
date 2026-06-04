package dyds.crypto.cecoin.presentation.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.presentation.utils.buildFallibleComposable
import dyds.crypto.cecoin.presentation.utils.buildOneTimeLoadableComposable
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

@Composable
fun ChartScreen(
    modifier: Modifier = Modifier,
    viewModel: LiveChartViewModel,
    onBack: () -> Unit
) {
    val state = remember { MutableStateFlow<Loadable<Flow<ChartState>>>(Loadable.Loading) }
    LaunchedEffect(Unit) {
        viewModel.loadPrices()
        viewModel.uiState.first()
        state.emit(Loadable.Loaded(viewModel.uiState))
    }

    buildOneTimeLoadableComposable(
        inner = LiveChartScreen(viewModel.symbol, onBack, onRetry = viewModel::loadPrices, viewModel),
        onCancel = onBack
    )(state, modifier)
}

@Composable
fun LiveChartScreen(
    symbol: String,
    onBack: () -> Unit,
    onRetry: () -> Unit = {},
    viewModel: LiveChartViewModel,
): Renderer<Flow<ChartState>> =
    { value, modifier ->
        val state by value.collectAsState(initial = Fallible.Success(PricePoints()))
        val isFavorite by viewModel.isFavorite.collectAsState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Binance Live Trades - $symbol",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onBack) {
                    Text("Back")
                }
            }

            buildFallibleComposable(
                inner = PriceChart(),
                onCancel = onBack,
                onRetry = onRetry
            )(state, modifier)

            FilledTonalButton(
                onClick = { viewModel.toggleFavorite() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (isFavorite) "\u2605 Remove from Favorites" else "\u2606 Add to Favorites",
                )
            }
        }
    }
