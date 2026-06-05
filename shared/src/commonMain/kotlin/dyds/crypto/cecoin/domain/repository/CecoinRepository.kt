package dyds.crypto.cecoin.domain.repository

import dyds.crypto.cecoin.domain.model.OrderBook
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow

interface CecoinRepository {
    suspend fun getAvailableSymbols(): List<CryptoSymbol>
    suspend fun getHistoricalPrices(symbol: String, interval: String = "1m", limit: Int = 200): List<TradePrice>
    fun observeTradePrices(symbol: String): Flow<TradePrice>
    fun observeOrderBook(symbol: String): Flow<OrderBook>
}


