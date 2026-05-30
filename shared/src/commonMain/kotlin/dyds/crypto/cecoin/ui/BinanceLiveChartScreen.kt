package dyds.crypto.cecoin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun BinanceLiveChartScreen(
    modifier: Modifier = Modifier,
    client: dyds.crypto.cecoin.binance.BinanceStreamClient? = null,
) {
    var symbol by remember { mutableStateOf("BTCUSDT") }
    var running by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("Idle") }

    val prices = remember { mutableStateListOf<Double>() }
    val maxPoints = 200

    LaunchedEffect(running, symbol, client) {
        if (client == null) {
            status = "Preview"
            prices.clear()
            repeat(120) { i ->
                val base = 100.0
                val v = base + (kotlin.math.sin(i / 10.0) * 5.0)
                prices.add(v)
            }
            return@LaunchedEffect
        }

        if (!running) {
            status = "Paused"
            return@LaunchedEffect
        }

        prices.clear()
        while (isActive && running) {
            try {
                status = "Connecting…"
                client.tradePrices(symbol).collect { price ->
                    status = "Connected"
                    if (prices.size >= maxPoints) prices.removeAt(0)
                    prices.add(price)
                }
                status = "Disconnected"
                delay(1_000)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                val msg = t.message?.take(80) ?: t::class.simpleName.orEmpty()
                status = "Error: $msg"
                delay(1_000)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Binance Live Trades",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = symbol,
                onValueChange = { symbol = it.uppercase() },
                label = { Text("Symbol") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { running = !running }) {
                Text(if (running) "Stop" else "Start")
            }
            Button(onClick = { prices.clear() }) {
                Text("Clear")
            }
        }

        Text("Status: $status")
        Text(
            text = "Last: ${prices.lastOrNull()?.toString() ?: "—"}",
            fontWeight = FontWeight.Medium,
        )

        PriceChart(prices = prices)
    }
}
