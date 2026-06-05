package dyds.crypto.cecoin.data.remote.coincap.proxy

import dyds.crypto.cecoin.data.remote.CoinPriceSource
import dyds.crypto.cecoin.data.remote.coincap.CoinCapPriceSource
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CoinCapPriceSourceProxy(
    private val source: CoinCapPriceSource,
) : CoinPriceSource {

    override fun tradePrices(symbol: String): Flow<TradePrice> =
        source.tradePrices(symbol).map { (price, timestamp) ->
            TradePrice(symbol, PricePoint(timestamp, price))
        }

    override fun close() {
        source.close()
    }
}
