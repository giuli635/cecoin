package dyds.crypto.cecoin.chart.domain.repository

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import kotlinx.coroutines.flow.Flow

interface PriceRepository {
    suspend fun getHistoricalPrices(symbol: String, interval: String = "1m", limit: Int = 200): List<PricePoint>
    fun observePrices(symbol: String): Flow<PricePoint>
}
