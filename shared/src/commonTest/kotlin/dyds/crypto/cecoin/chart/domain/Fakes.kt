package dyds.crypto.cecoin.chart.domain

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.repository.PriceRepository
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class FakePriceRepository(
    var historical: List<PricePoint> = emptyList(),
    var tradeFlow: Flow<PricePoint> = emptyFlow(),
    var historicalException: Throwable? = null,
) : PriceRepository {
    var lastSymbol: CryptoSymbol? = null
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun getHistoricalPrices(
        symbol: CryptoSymbol, interval: String, limit: Int,
    ): List<PricePoint> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        historicalException?.let { throw it }
        return historical
    }

    override fun observePrices(symbol: CryptoSymbol): Flow<PricePoint> {
        lastSymbol = symbol
        return tradeFlow
    }
}
