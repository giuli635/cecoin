package dyds.crypto.cecoin.data.remote.binance.proxy

import dyds.crypto.cecoin.data.remote.CoinOrderBookSource
import dyds.crypto.cecoin.data.remote.binance.BinanceOrderBookSource
import dyds.crypto.cecoin.domain.model.OrderBook

internal class BinanceOrderBookSourceProxy(
    private val source: BinanceOrderBookSource,
) : CoinOrderBookSource {

    override suspend fun fetchOrderBook(symbol: String): OrderBook? =
        runCatching { source.fetchOrderBook(symbol) }
            .getOrNull()
}
