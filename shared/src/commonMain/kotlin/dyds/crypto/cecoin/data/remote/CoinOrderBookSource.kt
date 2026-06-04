package dyds.crypto.cecoin.data.remote

import dyds.crypto.cecoin.domain.model.OrderBook

interface CoinOrderBookSource {
    suspend fun fetchOrderBook(symbol: String): OrderBook?
}
