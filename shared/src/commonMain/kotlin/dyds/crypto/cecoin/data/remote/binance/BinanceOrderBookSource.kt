package dyds.crypto.cecoin.data.remote.binance

import dyds.crypto.cecoin.data.remote.CoinOrderBookSource
import dyds.crypto.cecoin.domain.model.OrderBook
import dyds.crypto.cecoin.domain.model.OrderBookEntry
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json

internal class BinanceOrderBookSource : CoinOrderBookSource {
    private val http = HttpClient {
        install(WebSockets)
    }
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchOrderBook(symbol: String): OrderBook? {
        val stream = "${symbol.trim().lowercase()}@depth20"
        var remote: BinanceRemoteOrderBook? = null
        http.webSocket(urlString = "wss://stream.binance.com:9443/ws/$stream") {
            for (frame in incoming) {
                val text = (frame as? Frame.Text)?.readText() ?: continue
                remote = json.decodeFromString(text)
                break
            }
        }
        return remote?.toDomain()
    }

    private fun BinanceRemoteOrderBook.toDomain() = OrderBook(
        bids = bids.map { (price, qty) ->
            OrderBookEntry(price.toDouble(), qty.toDouble())
        },
        asks = asks.map { (price, qty) ->
            OrderBookEntry(price.toDouble(), qty.toDouble())
        },
    )
}
