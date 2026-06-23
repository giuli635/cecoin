package dyds.crypto.cecoin.core.data.caching

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CachedDataSource<T>(
    private val fetchBlock: suspend () -> List<T>,
    private val cacheTtl: Duration,
) {

    @Volatile
    private var cacheEntry: CacheEntry<T>? = null

    private val mutex = Mutex()

    suspend fun get(): List<T> {
        cacheEntry?.let { entry ->
            if (entry.timeMark.elapsedNow() < cacheTtl) return entry.value
        }

        return mutex.withLock {
            cacheEntry?.let { entry ->
                if (entry.timeMark.elapsedNow() < cacheTtl) return@withLock entry.value
            }

            val fresh = fetchBlock()
            cacheEntry = CacheEntry(fresh, TimeSource.Monotonic.markNow())
            fresh
        }
    }

    fun invalidate() {
        cacheEntry = null
    }

    private data class CacheEntry<T>(val value: List<T>, val timeMark: TimeMark)
}
