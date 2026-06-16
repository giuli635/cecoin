package dyds.crypto.cecoin.data.remote

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
private const val TRADING_STATUS = "TRADING"
private const val SYMBOLS_FIELD = "symbols"
private const val STATUS_FIELD = "status"
private const val QUOTE_ASSET_FIELD = "quoteAsset"
private const val BASE_ASSET_FIELD = "baseAsset"
private const val SYMBOL_FIELD = "symbol"

class BinanceCoinListDataSource(
    private val http: HttpClient,
) : CoinListDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchSymbols(): List<CryptoSymbol> {
        val url = "$BINANCE_API_URL/exchangeInfo"
        val responseText = http.get(url).body<String>()
        return parseSymbols(responseText)
    }

    private fun parseSymbols(jsonText: String): List<CryptoSymbol> {
        return runCatching {
            val root = json.parseToJsonElement(jsonText).jsonObject
            val symbolsArray = root[SYMBOLS_FIELD]?.jsonArray ?: return emptyList()
            symbolsArray.mapNotNull { jsonElementToCryptoSymbol(it) }.sortedBy { it.symbol }
        }.getOrElse { emptyList() }
    }

    private fun jsonElementToCryptoSymbol(element: kotlinx.serialization.json.JsonElement): CryptoSymbol? {
        return runCatching {
            val obj = element.jsonObject
            val status = obj[STATUS_FIELD]?.jsonPrimitive?.content ?: return@runCatching null
            val quoteAsset = obj[QUOTE_ASSET_FIELD]?.jsonPrimitive?.content ?: return@runCatching null
            if (status != TRADING_STATUS || quoteAsset != QUOTE_ASSET_FILTER) return@runCatching null
            CryptoSymbol(
                symbol = obj[SYMBOL_FIELD]!!.jsonPrimitive.content,
                baseAsset = obj[BASE_ASSET_FIELD]!!.jsonPrimitive.content,
                quoteAsset = quoteAsset,
                status = status
            )
        }.getOrNull()
    }
}
