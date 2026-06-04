package dyds.crypto.cecoin.data.remote.coincap.proxy

import dyds.crypto.cecoin.data.remote.CoinOrderBookSource
import dyds.crypto.cecoin.data.remote.coincap.CoinCapOrderBookSource
import dyds.crypto.cecoin.domain.model.OrderBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

internal class CoinCapOrderBookSourceProxy(
    private val source: CoinCapOrderBookSource,
) : CoinOrderBookSource {

    override fun observeOrderBook(symbol: String): Flow<OrderBook> =
        source.observeOrderBook(symbol)
            .catch { /* CoinCap falla silenciosamente, Binance es la primaria */ }
}
