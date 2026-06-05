package dyds.crypto.cecoin.data.remote

import dyds.crypto.cecoin.domain.model.TradePrice

interface CoinHistoricalSource {
    suspend fun getHistoricalPrices(symbol: String, interval: String = "1m", limit: Int = 200): List<TradePrice>
    fun close()
}
