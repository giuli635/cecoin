package dyds.crypto.cecoin.data.remote.coincap.proxy

import dyds.crypto.cecoin.data.remote.CoinPriceSource
import dyds.crypto.cecoin.data.remote.coincap.CoinCapPriceSource
import kotlinx.coroutines.flow.Flow

internal class CoinCapPriceSourceProxy(
    private val source: CoinCapPriceSource,
) : CoinPriceSource {

    override fun tradePrices(symbol: String): Flow<Double> = source.tradePrices(symbol)

    override fun close() {
        source.close()
    }
}
