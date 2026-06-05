package dyds.crypto.cecoin.data.remote.coincap

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
import kotlin.time.TimeSource

private const val BASE_URL = "wss://ws.coincap.io/prices"

class CoinCapPriceSource {
    private val http = HttpClient {
        install(WebSockets)
    }
    private val json = Json { ignoreUnknownKeys = true }

    private val startMark = TimeSource.Monotonic.markNow()

    fun tradePrices(symbol: String): Flow<Pair<Double, Long>> = flow {
        val assetId = symbolToAssetId(symbol)
        val url = "$BASE_URL?assets=$assetId"

        http.webSocket(urlString = url) {
            for (frame in incoming) {
                val text = (frame as? Frame.Text)?.readText() ?: continue
                val price = parsePrice(text, assetId) ?: continue
                emit(Pair(price, startMark.elapsedNow().inWholeMilliseconds))
            }
        }
    }

    private fun parsePrice(message: String, assetId: String): Double? =
        runCatching {
            val root = json.parseToJsonElement(message).jsonObject
            root[assetId]?.jsonPrimitive?.content?.toDouble()
        }.getOrNull()

    fun close() {
        http.close()
    }
}
