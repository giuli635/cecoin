package dyds.crypto.cecoin.search.data.repository

import dyds.crypto.cecoin.core.data.caching.CachedDataSource
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.data.datasource.CoinListDataSource
import dyds.crypto.cecoin.search.domain.repository.CryptoSymbolRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SearchRepositoryImpl(
    private val coinListDataSource: CoinListDataSource,
    cacheTtl: Duration = DEFAULT_CACHE_TTL,
) : CryptoSymbolRepository {

    private val cache = CachedDataSource(
        fetchBlock = coinListDataSource::fetchSymbols,
        cacheTtl = cacheTtl,
    )

    override suspend fun getAvailableSymbols(): List<CryptoSymbol> = cache.get()

    fun invalidateCache() = cache.invalidate()

    companion object {
        val DEFAULT_CACHE_TTL: Duration = 5.minutes
    }
}
