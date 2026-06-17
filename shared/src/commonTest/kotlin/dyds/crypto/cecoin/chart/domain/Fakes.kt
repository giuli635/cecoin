package dyds.crypto.cecoin.chart.domain

import dyds.crypto.cecoin.chart.domain.model.TradePrice
import dyds.crypto.cecoin.chart.domain.repository.TradePriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class FakeTradePriceRepository(
    var historical: List<TradePrice> = emptyList(),
    var tradeFlow: Flow<TradePrice> = emptyFlow(),
    var historicalException: Throwable? = null,
) : TradePriceRepository {
    var lastSymbol: String = ""
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun getHistoricalPrices(
        symbol: String, interval: String, limit: Int,
    ): List<TradePrice> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        historicalException?.let { throw it }
        return historical
    }

    override fun observeTradePrices(symbol: String): Flow<TradePrice> {
        lastSymbol = symbol
        return tradeFlow
    }
}
