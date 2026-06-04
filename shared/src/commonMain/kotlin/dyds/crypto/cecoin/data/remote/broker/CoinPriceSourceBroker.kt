package dyds.crypto.cecoin.data.remote.broker

import dyds.crypto.cecoin.data.remote.CoinPriceSource
import dyds.crypto.cecoin.data.remote.binance.proxy.BinancePriceSourceProxy
import dyds.crypto.cecoin.data.remote.coincap.proxy.CoinCapPriceSourceProxy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal class CoinPriceSourceBroker(
    private val binanceProxy: BinancePriceSourceProxy,
    private val coinCapProxy: CoinCapPriceSourceProxy,
) : CoinPriceSource {

    override fun tradePrices(symbol: String): Flow<Double> = flow {
        emitAll(
            binanceProxy.tradePrices(symbol)
                .catch { emitAll(coinCapProxy.tradePrices(symbol)) }
        )
    }

    override fun close() {
        binanceProxy.close()
        coinCapProxy.close()
    }
}
