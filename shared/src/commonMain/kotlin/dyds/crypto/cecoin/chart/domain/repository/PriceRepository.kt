package dyds.crypto.cecoin.chart.domain.repository

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow

interface PriceRepository {
    suspend fun getHistoricalPrices(symbol: CryptoSymbol, interval: String = "1m", limit: Int = 200): List<PricePoint>
    fun observePrices(symbol: CryptoSymbol): Flow<PricePoint>
}
