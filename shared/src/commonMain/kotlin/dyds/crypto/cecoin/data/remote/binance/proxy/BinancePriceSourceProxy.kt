package dyds.crypto.cecoin.data.remote.binance.proxy

import dyds.crypto.cecoin.data.remote.CoinPriceSource
import dyds.crypto.cecoin.data.remote.binance.BinanceCoinPriceSource
import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class BinancePriceSourceProxy(
    private val source: BinanceCoinPriceSource,
) : CoinPriceSource {

    override fun tradePrices(symbol: String): Flow<TradePrice> =
        source.tradePrices(symbol).map { price -> TradePrice(symbol = symbol, price = price) }

    override fun close() {
        source.close()
    }
}
