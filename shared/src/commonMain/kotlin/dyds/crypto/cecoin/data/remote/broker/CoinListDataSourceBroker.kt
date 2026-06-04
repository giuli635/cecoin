package dyds.crypto.cecoin.data.remote.broker

import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.data.remote.binance.proxy.BinanceCoinListDataSourceProxy
import dyds.crypto.cecoin.domain.model.CryptoSymbol

internal class CoinListDataSourceBroker(
    private val binanceProxy: BinanceCoinListDataSourceProxy,
) : CoinListDataSource {

    override suspend fun fetchSymbols(): List<CryptoSymbol>? =
        binanceProxy.fetchSymbols()
}
