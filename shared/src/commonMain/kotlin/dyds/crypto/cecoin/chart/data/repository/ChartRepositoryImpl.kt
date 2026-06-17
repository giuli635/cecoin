package dyds.crypto.cecoin.chart.data.repository

import dyds.crypto.cecoin.chart.data.datasource.CoinHistoricalDataSource
import dyds.crypto.cecoin.chart.data.datasource.CoinPriceDataSource
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow

private const val DefaultSymbol = "BTCUSDT"

class ChartRepositoryImpl(
    private val coinPriceSource: CoinPriceDataSource,
    private val coinHistoricalSource: CoinHistoricalDataSource,
) : PriceRepository {

    override suspend fun getHistoricalPrices(symbol: String, interval: String, limit: Int): List<PricePoint> =
        coinHistoricalSource.getHistoricalPrices(symbol.normalizeSymbol(), interval, limit)

    override fun observePrices(symbol: String): Flow<PricePoint> =
        coinPriceSource.observePrices(symbol.normalizeSymbol())
}

private fun String.normalizeSymbol(): String =
    trim().uppercase().ifBlank { DefaultSymbol }
