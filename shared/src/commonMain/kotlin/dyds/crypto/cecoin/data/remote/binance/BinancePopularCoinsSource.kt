package dyds.crypto.cecoin.data.remote.binance

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

private const val BINANCE_API_URL = "https://api.binance.com/api/v3"

class BinancePopularCoinsSource {
    private val http = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchTopSymbols(): List<String> {
        val url = "$BINANCE_API_URL/ticker/24hr"
        val responseText = http.get(url).body<String>()
        val tickers = json.decodeFromString<List<BinanceRemoteTicker>>(responseText)
        return tickers
            .filter { it.symbol.endsWith("USDT") }
            .sortedByDescending { it.quoteVolume.toDoubleOrNull() ?: 0.0 }
            .take(20)
            .map { it.symbol }
    }
}
