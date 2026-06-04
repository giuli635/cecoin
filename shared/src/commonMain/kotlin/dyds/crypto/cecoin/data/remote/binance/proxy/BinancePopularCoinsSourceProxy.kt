package dyds.crypto.cecoin.data.remote.binance.proxy

import dyds.crypto.cecoin.data.remote.binance.BinancePopularCoinsSource

internal class BinancePopularCoinsSourceProxy(
    private val source: BinancePopularCoinsSource,
) {
    suspend fun fetchTopSymbols(): List<String>? =
        runCatching { source.fetchTopSymbols() }
            .getOrNull()
}
