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
    private var cachedValue: List<T>? = null

    @Volatile
    private var lastFetchTimeMark: TimeMark? = null

    private val mutex = Mutex()

    suspend fun get(): List<T> {
        cachedValue?.let { value ->
            lastFetchTimeMark?.let { mark ->
                if (mark.elapsedNow() < cacheTtl) return value
            }
        }

        return mutex.withLock {
            cachedValue?.let { value ->
                lastFetchTimeMark?.let { mark ->
                    if (mark.elapsedNow() < cacheTtl) return@withLock value
                }
            }

            val fresh = fetchBlock()
            cachedValue = fresh
            lastFetchTimeMark = TimeSource.Monotonic.markNow()
            fresh
        }
    }

    fun invalidate() {
        lastFetchTimeMark = null
        cachedValue = null
    }
}
