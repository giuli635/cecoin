    package dyds.crypto.cecoin.chart.data.repository

import dyds.crypto.cecoin.chart.data.datasource.CoinHistoricalDataSource
import dyds.crypto.cecoin.chart.data.datasource.CoinPriceDataSource
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.repository.PriceRepository
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow

private val DEFAULT_SYMBOL = CryptoSymbol("BTCUSDT")

class PriceRepositoryImpl(
    private val coinPriceSource: CoinPriceDataSource,
    private val coinHistoricalSource: CoinHistoricalDataSource,
) : PriceRepository {

    override suspend fun getHistoricalPrices(symbol: CryptoSymbol, interval: String, limit: Int): List<PricePoint> =
        coinHistoricalSource.getHistoricalPrices(symbol.normalizeSymbol(), interval, limit)

    override fun observePrices(symbol: CryptoSymbol): Flow<PricePoint> =
        coinPriceSource.observePrices(symbol.normalizeSymbol())
}

private fun CryptoSymbol.normalizeSymbol(): CryptoSymbol =
    CryptoSymbol(symbol.trim().uppercase().ifBlank { DEFAULT_SYMBOL.symbol })
