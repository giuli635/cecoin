package dyds.crypto.cecoin.data.remote.binance.proxy

import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.data.remote.binance.BinanceCoinListDataSource
import dyds.crypto.cecoin.domain.model.CryptoSymbol

internal class BinanceCoinListDataSourceProxy(
    private val source: BinanceCoinListDataSource,
) : CoinListDataSource {

    override suspend fun fetchSymbols(): List<CryptoSymbol>? =
        runCatching { source.fetchSymbols() }
            .getOrNull()
}
