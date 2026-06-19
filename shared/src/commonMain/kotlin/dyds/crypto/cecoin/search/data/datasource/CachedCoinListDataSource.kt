package dyds.crypto.cecoin.search.data.datasource

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class CachedCoinListDataSource(
    private val source: CoinListDataSource,
    private val ttl: Duration = DEFAULT_TTL,
) : CoinListDataSource {

    @Volatile
    private var cachedSymbols: List<CryptoSymbol>? = null

    @Volatile
    private var lastFetchTimeMs: Long = NEVER_FETCHED

    private val mutex = Mutex()
    private val startMark = TimeSource.Monotonic.markNow()

    protected open fun now(): Long = startMark.elapsedNow().inWholeMilliseconds

    override suspend fun fetchSymbols(): List<CryptoSymbol> {
        cachedSymbols?.let { symbols ->
            if (lastFetchTimeMs != NEVER_FETCHED && now() - lastFetchTimeMs < ttl.inWholeMilliseconds)
                return symbols
        }

        return mutex.withLock {
            cachedSymbols?.let { symbols ->
                if (lastFetchTimeMs != NEVER_FETCHED && now() - lastFetchTimeMs < ttl.inWholeMilliseconds)
                    return@withLock symbols
            }

            val freshSymbols = source.fetchSymbols()
            cachedSymbols = freshSymbols
            lastFetchTimeMs = now()
            freshSymbols
        }
    }

    fun invalidateCache() {
        lastFetchTimeMs = NEVER_FETCHED
        cachedSymbols = null
    }

    companion object {
        val DEFAULT_TTL: Duration = 5.minutes
        private const val NEVER_FETCHED = -1L
    }
}
