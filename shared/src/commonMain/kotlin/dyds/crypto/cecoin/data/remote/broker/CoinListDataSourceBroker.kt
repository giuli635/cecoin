package dyds.crypto.cecoin.data.remote.broker

import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.data.remote.binance.proxy.BinanceCoinListDataSourceProxy
import dyds.crypto.cecoin.data.remote.binance.proxy.BinancePopularCoinsSourceProxy
import dyds.crypto.cecoin.domain.model.CryptoSymbol

internal class CoinListDataSourceBroker(
    private val exchangeInfoProxy: BinanceCoinListDataSourceProxy,
    private val popularCoinsProxy: BinancePopularCoinsSourceProxy,
) : CoinListDataSource {

    override suspend fun fetchSymbols(): List<CryptoSymbol>? {
        val popularSymbols = popularCoinsProxy.fetchTopSymbols()
        val allSymbols = exchangeInfoProxy.fetchSymbols()

        return when {
            popularSymbols != null && allSymbols != null ->
                allSymbols.filter { it.symbol in popularSymbols }
            popularSymbols != null ->
                allSymbols?.filter { it.symbol in popularSymbols }
            allSymbols != null -> allSymbols
            else -> null
        }
    }
}
