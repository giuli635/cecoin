package dyds.crypto.cecoin.chart.domain.repository

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.model.TradePrice
import kotlinx.coroutines.flow.Flow

interface TradePriceRepository {
    suspend fun getHistoricalPrices(symbol: String, interval: String = "1m", limit: Int = 200): List<PricePoint>
    fun observeTradePrices(symbol: String): Flow<TradePrice>
}
