package dyds.crypto.cecoin.data.remote.coincap

import dyds.crypto.cecoin.domain.model.OrderBook
import dyds.crypto.cecoin.domain.model.OrderBookEntry
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

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

private const val API_URL = "https://api.coincap.io/v2/markets"

class CoinCapOrderBookSource {
    private val http = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun observeOrderBook(symbol: String): Flow<OrderBook> = flow {
        val baseAsset = symbol.trim().uppercase().let { s ->
            s.removeSuffix("USDT").removeSuffix("USD").removeSuffix("BTC")
        }
        val assetId = SYMBOL_TO_ASSET_ID[baseAsset] ?: baseAsset.lowercase()
        while (true) {
            val response = http.get("$API_URL?baseId=$assetId&limit=20")
            val body = response.bodyAsText()
            val remote = json.decodeFromString<CoinCapMarketsResponse>(body)
            emit(remote.toDomain())
            delay(5.seconds)
        }
    }

    private fun CoinCapMarketsResponse.toDomain(): OrderBook {
        val entries = data.mapNotNull { market ->
            val price = market.priceUsd?.toDoubleOrNull() ?: return@mapNotNull null
            val volume = market.volumeUsd24Hr?.toDoubleOrNull() ?: return@mapNotNull null
            OrderBookEntry(
                price = price,
                quantity = volume / price,
            )
        }
        val mid = entries.size / 2
        val sortedByPrice = entries.sortedBy { it.price }
        return OrderBook(
            bids = sortedByPrice.take(mid).sortedByDescending { it.price }.take(20),
            asks = sortedByPrice.drop(mid).sortedBy { it.price }.take(20),
        )
    }
}
