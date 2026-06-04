package dyds.crypto.cecoin.data.remote

import dyds.crypto.cecoin.domain.model.OrderBook
import kotlinx.coroutines.flow.Flow

interface CoinOrderBookSource {
    fun observeOrderBook(symbol: String): Flow<OrderBook>
}
