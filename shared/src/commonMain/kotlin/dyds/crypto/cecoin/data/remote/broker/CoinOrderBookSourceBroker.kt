package dyds.crypto.cecoin.data.remote.broker

import dyds.crypto.cecoin.data.remote.CoinOrderBookSource
import dyds.crypto.cecoin.data.remote.binance.proxy.BinanceOrderBookSourceProxy
import dyds.crypto.cecoin.data.remote.coincap.proxy.CoinCapOrderBookSourceProxy
import dyds.crypto.cecoin.domain.model.OrderBook
import dyds.crypto.cecoin.domain.model.OrderBookEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class CoinOrderBookSourceBroker(
    private val binanceProxy: BinanceOrderBookSourceProxy,
    private val coinCapProxy: CoinCapOrderBookSourceProxy,
) : CoinOrderBookSource {

    override suspend fun fetchOrderBook(symbol: String): OrderBook? = coroutineScope {
        val binanceDeferred = async { binanceProxy.fetchOrderBook(symbol) }
        val coinCapDeferred = async { coinCapProxy.fetchOrderBook(symbol) }

        val binanceOrderBook = binanceDeferred.await()
        val coinCapOrderBook = coinCapDeferred.await()

        when {
            binanceOrderBook != null && coinCapOrderBook != null ->
                mergeOrderBooks(binanceOrderBook, coinCapOrderBook)
            binanceOrderBook != null -> binanceOrderBook
            coinCapOrderBook != null -> coinCapOrderBook
            else -> null
        }
    }

    private fun mergeOrderBooks(first: OrderBook, second: OrderBook): OrderBook = OrderBook(
        bids = (first.bids + second.bids)
            .sortedByDescending { it.price }
            .take(20),
        asks = (first.asks + second.asks)
            .sortedBy { it.price }
            .take(20),
    )
}
