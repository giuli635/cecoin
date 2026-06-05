package dyds.crypto.cecoin.data.remote.binance

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

class BinanceCoinPriceSource {
    private val http = HttpClient {
        install(WebSockets)
    }
    private val json = Json { ignoreUnknownKeys = true }

    fun tradePrices(symbol: String): Flow<Pair<Double, Long>> = flow {
        val stream = "${symbol.trim().lowercase()}@trade"
        var lastError: Throwable? = null

        for (baseUrl in BASE_URLS) {
            val url = "$baseUrl/ws/$stream"
            try {
                http.webSocket(urlString = url) {
                    for (frame in incoming) {
                        val text = (frame as? Frame.Text)?.readText() ?: continue
                        val parsed = parseTrade(text) ?: continue
                        emit(parsed)
                    }
                }
                return@flow
            } catch (throwable: Throwable) {
                lastError = throwable
            }
        }

        throw lastError ?: IllegalStateException("No se pudo abrir el WebSocket de Binance")
    }

    private fun parseTrade(message: String): Pair<Double, Long>? =
        runCatching {
            val root = json.parseToJsonElement(message).jsonObject
            val price = root["p"]?.jsonPrimitive?.content?.toDouble() ?: return@runCatching null
            val timestamp = root["T"]?.jsonPrimitive?.content?.toLong() ?: return@runCatching null
            Pair(price, timestamp)
        }.getOrNull()

    fun close() {
        http.close()
    }
}
