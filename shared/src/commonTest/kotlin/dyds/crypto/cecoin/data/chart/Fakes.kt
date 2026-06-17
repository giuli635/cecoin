package dyds.crypto.cecoin.data.chart

import dyds.crypto.cecoin.data.chart.datasource.CoinHistoricalDataSource
import dyds.crypto.cecoin.data.chart.datasource.CoinPriceDataSource
import dyds.crypto.cecoin.domain.chart.model.TradePrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class FakeCoinHistoricalSource(
    private val prices: List<TradePrice> = emptyList(),
    var exception: Throwable? = null,
) : CoinHistoricalDataSource {
    var lastSymbol: String = ""
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun getHistoricalPrices(
        symbol: String, interval: String, limit: Int,
    ): List<TradePrice> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        exception?.let { throw it }
        return prices
    }
}

internal class FakeCoinPriceSource(
    private val flow: Flow<TradePrice> = emptyFlow(),
) : CoinPriceDataSource {
    var lastSymbol: String = ""

    override fun tradePrices(symbol: String): Flow<TradePrice> {
        lastSymbol = symbol
        return flow
    }
}
