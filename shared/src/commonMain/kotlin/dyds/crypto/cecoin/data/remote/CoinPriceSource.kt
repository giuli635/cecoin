package dyds.crypto.cecoin.data.remote

import kotlinx.coroutines.flow.Flow

interface CoinPriceSource {
    fun tradePrices(symbol: String): Flow<Double>
    fun close()
}

