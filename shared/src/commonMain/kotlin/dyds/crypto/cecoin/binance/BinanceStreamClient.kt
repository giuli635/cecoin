package dyds.crypto.cecoin.binance

import kotlinx.coroutines.flow.Flow

interface BinanceStreamClient {
    fun tradePrices(symbol: String): Flow<Double>
    fun close()
}
