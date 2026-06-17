package dyds.crypto.cecoin.chart.data.datasource

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import dyds.crypto.cecoin.core.utils.ErrorStrings

private val BASE_URLS = listOf(
    "wss://stream.binance.com:9443",
    "wss://stream.binance.com:443",
    "wss://data-stream.binance.vision",
)
private const val STREAM_SUFFIX = "@trade"

class BinanceCoinPriceDataSource(
    private val http: HttpClient,
) : CoinPriceDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override fun observePrices(symbol: CryptoSymbol): Flow<PricePoint> = flow {
        val stream = "${symbol.symbol.trim().lowercase()}$STREAM_SUFFIX"
        var lastError: Throwable? = null

        for (baseUrl in BASE_URLS) {
            val url = "$baseUrl/ws/$stream"
            try {
                http.webSocket(urlString = url) {
                    for (frame in incoming) {
                        val text = (frame as? Frame.Text)?.readText() ?: continue
                        val point = parseTrade(text) ?: continue
                        emit(point)
                    }
                }
                return@flow
            } catch (throwable: Throwable) {
                lastError = throwable
            }
        }

        throw lastError ?: IllegalStateException(ErrorStrings.WEBSOCKET_CONNECT)
    }

    private fun parseTrade(message: String): PricePoint? =
        runCatching {
            val root = json.parseToJsonElement(message).jsonObject
            val price = root["p"]?.jsonPrimitive?.content?.toDouble() ?: return@runCatching null
            val timestamp = root["T"]?.jsonPrimitive?.content?.toLong() ?: return@runCatching null
            PricePoint(timestamp, price)
        }.getOrNull()
}
