package dyds.crypto.cecoin.chart.data

import dyds.crypto.cecoin.chart.data.datasource.CoinHistoricalDataSource
import dyds.crypto.cecoin.chart.data.datasource.CoinPriceDataSource
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class FakeCoinHistoricalSource(
    private val prices: List<PricePoint> = emptyList(),
    var exception: Throwable? = null,
) : CoinHistoricalDataSource {
    var lastSymbol: String = ""
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun getHistoricalPrices(
        symbol: String, interval: String, limit: Int,
    ): List<PricePoint> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        exception?.let { throw it }
        return prices
    }
}

internal class FakeCoinPriceSource(
    private val flow: Flow<PricePoint> = emptyFlow(),
) : CoinPriceDataSource {
    var lastSymbol: String = ""

    override fun observePrices(symbol: String): Flow<PricePoint> {
        lastSymbol = symbol
        return flow
    }
}
