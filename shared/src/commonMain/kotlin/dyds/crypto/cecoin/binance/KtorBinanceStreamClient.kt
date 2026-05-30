package dyds.crypto.cecoin.binance

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class KtorBinanceStreamClient(
    private val http: HttpClient,
    private val baseUrl: String = "wss://stream.binance.com:9443",
) : BinanceStreamClient {

    private val json = Json { ignoreUnknownKeys = true }

    override fun tradePrices(symbol: String): Flow<Double> = flow {
        val stream = "${symbol.trim().lowercase()}@trade"
        val url = "$baseUrl/ws/$stream"

        http.webSocket(urlString = url) {
            for (frame in incoming) {
                val text = (frame as? Frame.Text)?.readText() ?: continue
                val price = parseTradePrice(text) ?: continue
                emit(price)
            }
        }
    }

    private fun parseTradePrice(message: String): Double? =
        runCatching {
            val root = json.parseToJsonElement(message).jsonObject
            root["p"]?.jsonPrimitive?.content?.toDouble()
        }.getOrNull()

    override fun close() {
        http.close()
    }
}
