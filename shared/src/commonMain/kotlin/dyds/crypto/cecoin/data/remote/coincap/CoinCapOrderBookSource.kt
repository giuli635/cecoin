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

private const val API_URL = "https://api.coincap.io/v2/markets"

class CoinCapOrderBookSource {
    private val http = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun observeOrderBook(symbol: String): Flow<OrderBook> = flow {
        val assetId = symbolToAssetId(symbol)
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
