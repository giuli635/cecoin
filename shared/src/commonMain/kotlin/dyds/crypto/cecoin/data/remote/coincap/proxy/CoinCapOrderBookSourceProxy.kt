package dyds.crypto.cecoin.data.remote.coincap.proxy

import dyds.crypto.cecoin.data.remote.CoinOrderBookSource
import dyds.crypto.cecoin.data.remote.coincap.CoinCapOrderBookSource
import dyds.crypto.cecoin.domain.model.OrderBook

internal class CoinCapOrderBookSourceProxy(
    private val source: CoinCapOrderBookSource,
) : CoinOrderBookSource {

    override suspend fun fetchOrderBook(symbol: String): OrderBook? =
        runCatching { source.fetchOrderBook(symbol) }
            .getOrNull()
}
