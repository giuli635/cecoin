package dyds.crypto.cecoin.domain.chart.repository

import dyds.crypto.cecoin.domain.chart.model.TradePrice
import kotlinx.coroutines.flow.Flow

interface TradePriceRepository {
    suspend fun getHistoricalPrices(symbol: String, interval: String = "1m", limit: Int = 200): List<TradePrice>
    fun observeTradePrices(symbol: String): Flow<TradePrice>
}
