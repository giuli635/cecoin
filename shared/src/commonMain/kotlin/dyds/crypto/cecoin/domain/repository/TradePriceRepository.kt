package dyds.crypto.cecoin.domain.repository

import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.flow.Flow

interface TradePriceRepository {
    fun observeTradePrices(symbol: String): Flow<TradePrice>
}


