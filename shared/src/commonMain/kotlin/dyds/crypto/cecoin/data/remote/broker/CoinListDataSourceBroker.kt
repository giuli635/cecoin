package dyds.crypto.cecoin.data.remote.broker

import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.data.remote.binance.proxy.BinanceCoinListDataSourceProxy
import dyds.crypto.cecoin.domain.model.CryptoSymbol

internal class CoinListDataSourceBroker(
    private val exchangeInfoProxy: BinanceCoinListDataSourceProxy,
) : CoinListDataSource {

    override suspend fun fetchSymbols(): List<CryptoSymbol>? =
        exchangeInfoProxy.fetchSymbols()
}
