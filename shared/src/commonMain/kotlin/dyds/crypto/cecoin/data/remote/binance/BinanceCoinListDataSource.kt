package dyds.crypto.cecoin.data.remote.binance

import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val BINANCE_API_URL = "https://api.binance.com/api/v3"
private const val QUOTE_ASSET_FILTER = "USDT"

class BinanceCoinListDataSource : CoinListDataSource {
    private val http = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchSymbols(): List<CryptoSymbol> {
        return try {
            val url = "$BINANCE_API_URL/exchangeInfo"
            val responseText = http.get(url).body<String>()
            parseSymbols(responseText)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun parseSymbols(jsonText: String): List<CryptoSymbol> {
        return runCatching {
            val root = json.parseToJsonElement(jsonText).jsonObject
            val symbolsArray = root["symbols"]?.jsonArray ?: return emptyList()

            symbolsArray.mapNotNull { element ->
                runCatching {
                    val obj = element.jsonObject
                    val status = obj["status"]?.jsonPrimitive?.content ?: return@runCatching null
                    val quoteAsset = obj["quoteAsset"]?.jsonPrimitive?.content ?: return@runCatching null

                    if (status == "TRADING" && quoteAsset == QUOTE_ASSET_FILTER) {
                        CryptoSymbol(
                            symbol = obj["symbol"]!!.jsonPrimitive.content,
                            baseAsset = obj["baseAsset"]!!.jsonPrimitive.content,
                            quoteAsset = quoteAsset,
                            status = status
                        )
                    } else {
                        null
                    }
                }.getOrNull()
            }.sortedBy { it.symbol }
        }.getOrElse { emptyList() }
    }
}
