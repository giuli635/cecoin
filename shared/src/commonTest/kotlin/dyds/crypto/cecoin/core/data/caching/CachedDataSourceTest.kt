package dyds.crypto.cecoin.core.data.caching

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds

class CachedDataSourceTest {

    @Test
    fun `get invokes fetchBlock on first call and returns data`() = runTest {
        var callCount = 0
        val source = CachedDataSource(
            fetchBlock = {
                callCount++
                listOf("a", "b")
            },
            cacheTtl = 5.minutes,
        )

        val result = source.get()

        assertEquals(listOf("a", "b"), result)
        assertEquals(1, callCount)
    }

    @Test
    fun `get returns cached data without invoking fetchBlock on second call`() = runTest {
        var callCount = 0
        val source = CachedDataSource(
            fetchBlock = {
                callCount++
                listOf("a", "b")
            },
            cacheTtl = 5.minutes,
        )

        source.get()
        val result = source.get()

        assertEquals(listOf("a", "b"), result)
        assertEquals(1, callCount)
    }

    @Test
    fun `get calls fetchBlock again when cache is invalidated`() = runTest {
        var callCount = 0
        val source = CachedDataSource(
            fetchBlock = {
                callCount++
                listOf("a")
            },
            cacheTtl = 5.minutes,
        )

        source.get()
        assertEquals(1, callCount)

        source.invalidate()
        source.get()

        assertEquals(2, callCount)
    }

    @Test
    fun `get returns latest data after invalidation`() = runTest {
        var data = listOf("a")
        val source = CachedDataSource(
            fetchBlock = { data },
            cacheTtl = 5.minutes,
        )

        source.get()
        data = listOf("b")

        source.invalidate()
        val result = source.get()

        assertEquals(listOf("b"), result)
    }

    @Test
    fun `concurrent calls only trigger one fetchBlock invocation`() = runTest {
        var callCount = 0
        val source = CachedDataSource(
            fetchBlock = {
                callCount++
                listOf("result")
            },
            cacheTtl = 5.minutes,
        )

        coroutineScope {
            val jobs = (1..10).map {
                launch { source.get() }
            }
            jobs.joinAll()
        }

        assertEquals(1, callCount)
    }

    @Test
    fun `get returns empty list when fetchBlock returns empty`() = runTest {
        val source = CachedDataSource<String>(
            fetchBlock = { emptyList() },
            cacheTtl = 5.minutes,
        )

        val result = source.get()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `get propagates exception from fetchBlock`() = runTest {
        val source = CachedDataSource<String>(
            fetchBlock = { throw RuntimeException("source fail") },
            cacheTtl = 5.minutes,
        )

        assertFailsWith<RuntimeException> {
            source.get()
        }
    }

    @Test
    fun `get re-fetches when TTL expires`() = runTest {
        var callCount = 0
        val source = CachedDataSource(
            fetchBlock = {
                callCount++
                listOf("a")
            },
            cacheTtl = 0.nanoseconds,
        )

        source.get()
        source.get()

        assertEquals(2, callCount)
    }

    @Test
    fun `second check inside mutex returns cached value from concurrent caller`() = runTest {
        var callCount = 0
        var secondResult: List<String>? = null
        val fetchStarted = CompletableDeferred<Unit>()
        val releaseFetch = CompletableDeferred<Unit>()
        val source = CachedDataSource(
            fetchBlock = {
                fetchStarted.complete(Unit)
                releaseFetch.await()
                callCount++
                listOf("a")
            },
            cacheTtl = 5.minutes,
        )

        val first = launch { source.get() }
        fetchStarted.await()

        val second = launch { secondResult = source.get() }

        releaseFetch.complete(Unit)

        first.join()
        second.join()

        assertEquals(1, callCount)
        assertEquals(listOf("a"), secondResult)
    }

    @Test
    fun `get retries fetchBlock after exception`() = runTest {
        var callCount = 0
        val source = CachedDataSource(
            fetchBlock = {
                callCount++
                if (callCount == 1) throw RuntimeException("first fail")
                listOf("ok")
            },
            cacheTtl = 5.minutes,
        )

        assertFailsWith<RuntimeException> { source.get() }
        assertEquals(1, callCount)

        val result = source.get()
        assertEquals(listOf("ok"), result)
        assertEquals(2, callCount)
    }
}
