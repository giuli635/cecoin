package dyds.crypto.cecoin.data.chart.repository

import dyds.crypto.cecoin.data.chart.datasource.CoinHistoricalDataSource
import dyds.crypto.cecoin.data.chart.datasource.CoinPriceDataSource
import dyds.crypto.cecoin.domain.chart.model.TradePrice
import dyds.crypto.cecoin.domain.chart.repository.TradePriceRepository
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
