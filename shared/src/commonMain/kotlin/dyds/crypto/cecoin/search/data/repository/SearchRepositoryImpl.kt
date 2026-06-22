package dyds.crypto.cecoin.search.data.repository

import dyds.crypto.cecoin.core.data.caching.CachedDataSource
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.data.datasource.CoinListDataSource
import dyds.crypto.cecoin.search.domain.repository.CryptoSymbolRepository

class SearchRepositoryImpl(
    private val coinListDataSource: CoinListDataSource,
    private val cache: CachedDataSource<CryptoSymbol>,
) : CryptoSymbolRepository {

    override suspend fun getAvailableSymbols(): List<CryptoSymbol> = cache.get()

    fun invalidateCache() = cache.invalidate()
}
