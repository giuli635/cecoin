package dyds.crypto.cecoin.domain.repository

import dyds.crypto.cecoin.domain.model.OrderBook
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow

interface CecoinRepository {
    suspend fun getAvailableSymbols(): List<CryptoSymbol>
    fun observeTradePrices(symbol: String): Flow<TradePrice>
    fun observeOrderBook(symbol: String): Flow<OrderBook>
}


