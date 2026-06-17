package dyds.crypto.cecoin.chart.data.repository

import dyds.crypto.cecoin.chart.data.datasource.CoinHistoricalDataSource
import dyds.crypto.cecoin.chart.data.datasource.CoinPriceDataSource
import dyds.crypto.cecoin.chart.domain.model.TradePrice
import dyds.crypto.cecoin.chart.domain.repository.TradePriceRepository
import kotlinx.coroutines.flow.Flow

private const val DefaultSymbol = "BTCUSDT"

class ChartRepositoryImpl(
    private val coinPriceSource: CoinPriceDataSource,
    private val coinHistoricalSource: CoinHistoricalDataSource,
) : TradePriceRepository {

    override suspend fun getHistoricalPrices(symbol: String, interval: String, limit: Int): List<TradePrice> =
        coinHistoricalSource.getHistoricalPrices(symbol.normalizeSymbol(), interval, limit)

    override fun observeTradePrices(symbol: String): Flow<TradePrice> =
        coinPriceSource.tradePrices(symbol.normalizeSymbol())
}

private fun String.normalizeSymbol(): String =
    trim().uppercase().ifBlank { DefaultSymbol }
