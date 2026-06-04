package dyds.crypto.cecoin.data.remote.binance.proxy

import dyds.crypto.cecoin.data.remote.CoinOrderBookSource
import dyds.crypto.cecoin.data.remote.binance.BinanceOrderBookSource
import dyds.crypto.cecoin.domain.model.OrderBook
import kotlinx.coroutines.flow.Flow

internal class BinanceOrderBookSourceProxy(
    private val source: BinanceOrderBookSource,
) : CoinOrderBookSource {

    override fun observeOrderBook(symbol: String): Flow<OrderBook> =
        source.observeOrderBook(symbol)
}
