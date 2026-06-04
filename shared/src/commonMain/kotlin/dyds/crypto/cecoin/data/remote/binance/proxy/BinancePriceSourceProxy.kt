package dyds.crypto.cecoin.data.remote.binance.proxy

import dyds.crypto.cecoin.data.remote.binance.BinanceCoinPriceSource
import dyds.crypto.cecoin.data.remote.CoinPriceSource
import kotlinx.coroutines.flow.Flow

internal class BinancePriceSourceProxy(
    private val source: BinanceCoinPriceSource,
) : CoinPriceSource {

    override fun tradePrices(symbol: String): Flow<Double> = source.tradePrices(symbol)

    override fun close() {
        source.close()
    }
}
