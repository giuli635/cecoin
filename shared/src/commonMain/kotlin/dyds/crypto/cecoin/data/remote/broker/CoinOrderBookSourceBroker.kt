package dyds.crypto.cecoin.data.remote.broker

import dyds.crypto.cecoin.data.remote.CoinOrderBookSource
import dyds.crypto.cecoin.data.remote.binance.proxy.BinanceOrderBookSourceProxy
import dyds.crypto.cecoin.data.remote.coincap.proxy.CoinCapOrderBookSourceProxy
import dyds.crypto.cecoin.domain.model.OrderBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal class CoinOrderBookSourceBroker(
    private val binanceProxy: BinanceOrderBookSourceProxy,
    private val coinCapProxy: CoinCapOrderBookSourceProxy,
) : CoinOrderBookSource {

    override fun observeOrderBook(symbol: String): Flow<OrderBook> = combine(
        binanceProxy.observeOrderBook(symbol)
            .map<OrderBook, OrderBook?> { it }
            .onStart { emit(null) }
            .catch { emit(null) },
        coinCapProxy.observeOrderBook(symbol)
            .map<OrderBook, OrderBook?> { it }
            .onStart { emit(null) }
            .catch { emit(null) },
    ) { binance, coinCap ->
        when {
            binance != null && coinCap != null -> mergeOrderBooks(binance, coinCap)
            binance != null -> binance
            coinCap != null -> coinCap
            else -> null
        }
    }.filterNotNull()

    private fun mergeOrderBooks(first: OrderBook, second: OrderBook): OrderBook = OrderBook(
        bids = (first.bids + second.bids)
            .sortedByDescending { it.price }
            .take(20),
        asks = (first.asks + second.asks)
            .sortedBy { it.price }
            .take(20),
    )
}
