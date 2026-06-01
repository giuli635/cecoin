package dyds.crypto.cecoin.data.remote

import kotlinx.coroutines.flow.Flow

interface BinanceStreamClient {
    fun tradePrices(symbol: String): Flow<Double>
    fun close()
}

