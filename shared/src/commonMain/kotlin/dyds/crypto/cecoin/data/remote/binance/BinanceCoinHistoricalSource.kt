package dyds.crypto.cecoin.data.remote.binance

import dyds.crypto.cecoin.data.remote.CoinHistoricalSource
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class BinanceCoinHistoricalSource : CoinHistoricalSource {
    private val http = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getHistoricalPrices(symbol: String, interval: String, limit: Int): List<TradePrice> {
        val url = "https://api.binance.com/api/v3/klines?" +
            "symbol=${symbol.uppercase()}&interval=$interval&limit=$limit"
        val raw: String = http.get(url).bodyAsText()
        val klines = json.parseToJsonElement(raw).jsonArray

        return klines.map { kline ->
            val arr = kline.jsonArray
            TradePrice(
                symbol,
                PricePoint(
                    timestamp = arr[0].jsonPrimitive.content.toLong(),
                    price = arr[4].jsonPrimitive.content.toDouble(),
                ),
            )
        }
    }

    override fun close() {
        http.close()
    }
}
