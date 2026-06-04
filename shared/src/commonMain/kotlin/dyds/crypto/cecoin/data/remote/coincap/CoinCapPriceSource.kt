package dyds.crypto.cecoin.data.remote.coincap

import dyds.crypto.cecoin.data.remote.CoinPriceSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val SYMBOL_TO_ASSET_ID = mapOf(
    "BTC" to "bitcoin",
    "ETH" to "ethereum",
    "USDT" to "tether",
    "BNB" to "binance-coin",
    "SOL" to "solana",
    "XRP" to "xrp",
    "ADA" to "cardano",
    "DOGE" to "dogecoin",
    "DOT" to "polkadot",
    "MATIC" to "matic-network",
)

private const val BASE_URL = "wss://ws.coincap.io/prices"

internal class CoinCapPriceSource : CoinPriceSource {
    private val http = HttpClient {
        install(WebSockets)
    }
    private val json = Json { ignoreUnknownKeys = true }

    override fun tradePrices(symbol: String): Flow<Double> = flow {
        val baseAsset = symbol.trim().uppercase().let { s ->
            s.removeSuffix("USDT").removeSuffix("USD").removeSuffix("BTC")
        }
        val assetId = SYMBOL_TO_ASSET_ID[baseAsset] ?: baseAsset.lowercase()
        val url = "$BASE_URL?assets=$assetId"

        http.webSocket(urlString = url) {
            for (frame in incoming) {
                val text = (frame as? Frame.Text)?.readText() ?: continue
                val price = parsePrice(text, assetId) ?: continue
                emit(price)
            }
        }
    }

    private fun parsePrice(message: String, assetId: String): Double? =
        runCatching {
            val root = json.parseToJsonElement(message).jsonObject
            root[assetId]?.jsonPrimitive?.content?.toDouble()
        }.getOrNull()

    override fun close() {
        http.close()
    }
}
