package dyds.crypto.cecoin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.BinanceLiveChartViewModel
import dyds.crypto.cecoin.presentation.ConnectionState

@Composable
fun BinanceLiveChartScreen(
    modifier: Modifier = Modifier,
    viewModel: BinanceLiveChartViewModel,
    onBackToSearch: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Binance Live Trades - ${state.symbol}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = onBackToSearch) {
                Text("Back")
            }
        }


        Text("Status: ${when (state.connectionState) {
            ConnectionState.Loading -> "Loading..."
            ConnectionState.Connected -> "Connected"
        }}")
        Text(
            text = "Last: ${state.lastPrice?.toString() ?: "—"}",
            fontWeight = FontWeight.Medium,
        )

        PriceChart(prices = state.prices)
    }
}
