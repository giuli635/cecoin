package dyds.crypto.cecoin.data.remote

import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
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

private val BASE_URLS = listOf(
    "wss://stream.binance.com:9443",
    "wss://stream.binance.com:443",
    "wss://data-stream.binance.vision",
)
private const val WEBSOCKET_CONNECT_ERROR = "No se pudo abrir el WebSocket de Binance"
private const val STREAM_SUFFIX = "@trade"

class BinanceCoinPriceDataSource(
    private val http: HttpClient,
) : CoinPriceDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override fun tradePrices(symbol: String): Flow<TradePrice> = flow {
        val stream = "${symbol.trim().lowercase()}$STREAM_SUFFIX"
        var lastError: Throwable? = null

        for (baseUrl in BASE_URLS) {
            val url = "$baseUrl/ws/$stream"
            try {
                http.webSocket(urlString = url) {
                    for (frame in incoming) {
                        val text = (frame as? Frame.Text)?.readText() ?: continue
                        val trade = parseTrade(text) ?: continue
                        emit(TradePrice(trade.symbol, PricePoint(trade.timestamp, trade.price)))
                    }
                }
                return@flow
            } catch (throwable: Throwable) {
                lastError = throwable
            }
        }

        throw lastError ?: IllegalStateException(WEBSOCKET_CONNECT_ERROR)
    }

    private fun parseTrade(message: String): TradePrice? =
        runCatching {
            val root = json.parseToJsonElement(message).jsonObject
            val price = root["p"]?.jsonPrimitive?.content?.toDouble() ?: return@runCatching null
            val timestamp = root["T"]?.jsonPrimitive?.content?.toLong() ?: return@runCatching null
            val symbol = root["s"]?.jsonPrimitive?.content ?: return@runCatching null
            TradePrice(symbol, PricePoint(timestamp, price))
        }.getOrNull()

}
