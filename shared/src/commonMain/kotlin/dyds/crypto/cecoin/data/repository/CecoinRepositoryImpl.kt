package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.remote.CoinHistoricalSource
import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.data.remote.CoinPriceSource
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.CecoinRepository
import kotlinx.coroutines.flow.Flow

private const val DefaultSymbol = "BTCUSDT"

class CecoinRepositoryImpl(
    private val coinPriceSource: CoinPriceSource,
    private val coinHistoricalSource: CoinHistoricalSource,
    private val coinListDataSource: CoinListDataSource,
) : CecoinRepository {

    override suspend fun getAvailableSymbols(): List<CryptoSymbol> =
        coinListDataSource.fetchSymbols()

    override suspend fun getHistoricalPrices(symbol: String, interval: String, limit: Int): List<TradePrice> =
        coinHistoricalSource.getHistoricalPrices(symbol.normalizeSymbol(), interval, limit)

    override fun observeTradePrices(symbol: String): Flow<TradePrice> =
        coinPriceSource.tradePrices(symbol.normalizeSymbol())
}

private fun String.normalizeSymbol(): String =
    trim().uppercase().ifBlank { DefaultSymbol }
