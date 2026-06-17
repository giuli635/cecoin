package dyds.crypto.cecoin.chart.data.datasource

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.model.TradePrice
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private const val BINANCE_API_URL = "https://api.binance.com/api/v3"
private const val KLINE_OPEN_TIME_INDEX = 0
private const val KLINE_CLOSE_PRICE_INDEX = 4

class BinanceCoinHistoricalDataSource(
    private val http: HttpClient,
) : CoinHistoricalDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getHistoricalPrices(symbol: String, interval: String, limit: Int): List<TradePrice> {
        val url = "$BINANCE_API_URL/klines?symbol=${symbol.uppercase()}&interval=$interval&limit=$limit"
        val raw: String = http.get(url).bodyAsText()
        val klines = json.parseToJsonElement(raw).jsonArray

        return klines.map { kline ->
            val arr = kline.jsonArray
            TradePrice(
                symbol,
                PricePoint(
                    timestamp = arr[KLINE_OPEN_TIME_INDEX].jsonPrimitive.content.toLong(),
                    price = arr[KLINE_CLOSE_PRICE_INDEX].jsonPrimitive.content.toDouble(),
                ),
            )
        }
    }

}
