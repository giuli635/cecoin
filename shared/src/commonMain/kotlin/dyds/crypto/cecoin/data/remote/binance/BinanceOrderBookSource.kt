package dyds.crypto.cecoin.data.remote.binance

import dyds.crypto.cecoin.domain.model.OrderBook
import dyds.crypto.cecoin.domain.model.OrderBookEntry
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

private val BASE_URLS = listOf(
    "wss://stream.binance.com:9443",
    "wss://stream.binance.com:443",
    "wss://data-stream.binance.vision",
)

class BinanceOrderBookSource {
    private val http = HttpClient {
        install(WebSockets)
    }
    private val json = Json { ignoreUnknownKeys = true }

    fun observeOrderBook(symbol: String): Flow<OrderBook> = flow {
        val stream = "${symbol.trim().lowercase()}@depth20"
        var lastError: Throwable? = null
        for (baseUrl in BASE_URLS) {
            val url = "$baseUrl/ws/$stream"
            try {
                http.webSocket(urlString = url) {
                    for (frame in incoming) {
                        val text = (frame as? Frame.Text)?.readText() ?: continue
                        val remote = json.decodeFromString<BinanceRemoteOrderBook>(text)
                        emit(remote.toDomain())
                    }
                }
                return@flow
            } catch (throwable: Throwable) {
                lastError = throwable
            }
        }
        throw lastError ?: IllegalStateException("No se pudo abrir el WebSocket de Binance")
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
