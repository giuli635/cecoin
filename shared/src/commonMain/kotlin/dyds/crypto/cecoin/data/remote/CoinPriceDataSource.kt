package dyds.crypto.cecoin.data.remote

import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.flow.Flow

interface CoinPriceDataSource {
    fun tradePrices(symbol: String): Flow<TradePrice>
    fun close()
}

