package dyds.crypto.cecoin.search.data.datasource

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CachedCoinListDataSource(
    private val source: CoinListDataSource,
    private val ttl: Duration = DEFAULT_TTL,
) : CoinListDataSource {

    @Volatile
    private var cachedSymbols: List<CryptoSymbol>? = null

    @Volatile
    private var lastFetchTimeMark: TimeMark? = null

    private val mutex = Mutex()

    override suspend fun fetchSymbols(): List<CryptoSymbol> {
        cachedSymbols?.let { symbols ->
            lastFetchTimeMark?.let { mark ->
                if (mark.elapsedNow() < ttl) return symbols
            }
        }

        return mutex.withLock {
            cachedSymbols?.let { symbols ->
                lastFetchTimeMark?.let { mark ->
                    if (mark.elapsedNow() < ttl) return@withLock symbols
                }
            }

            val freshSymbols = source.fetchSymbols()
            cachedSymbols = freshSymbols
            lastFetchTimeMark = TimeSource.Monotonic.markNow()
            freshSymbols
        }
    }

    fun invalidateCache() {
        lastFetchTimeMark = null
        cachedSymbols = null
    }

    companion object {
        val DEFAULT_TTL: Duration = 5.minutes
    }
}
