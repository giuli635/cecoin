package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.data.remote.CoinOrderBookSource
import dyds.crypto.cecoin.data.remote.CoinPriceSource
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.model.OrderBook
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.CecoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DefaultSymbol = "BTCUSDT"

class CryptoRepositoryImpl(
    private val coinPriceSource: CoinPriceSource,
    private val coinOrderBookSource: CoinOrderBookSource,
    private val coinListDataSource: CoinListDataSource,
) : CecoinRepository {

    override suspend fun getAvailableSymbols(): List<CryptoSymbol> =
        coinListDataSource.fetchSymbols()

    override fun observeTradePrices(symbol: String): Flow<TradePrice> {
        val normalizedSymbol = symbol.normalizeSymbol()
        return coinPriceSource
            .tradePrices(normalizedSymbol)
            .map { price -> TradePrice(symbol = normalizedSymbol, price = price) }
    }

    override suspend fun getOrderBook(symbol: String): OrderBook =
        coinOrderBookSource.fetchOrderBook(symbol.normalizeSymbol())
}

private fun String.normalizeSymbol(): String =
    trim().uppercase().ifBlank { DefaultSymbol }
