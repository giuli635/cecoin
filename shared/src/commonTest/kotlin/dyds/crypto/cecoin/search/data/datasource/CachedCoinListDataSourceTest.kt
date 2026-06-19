package dyds.crypto.cecoin.search.data.datasource

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.FakeTimeProvider
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.core.utils.fakeEthSymbol
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CachedCoinListDataSourceTest {

    @Test
    fun `fetchSymbols delegates to source on first call`() = runTest {
        val source = FakeCoinListDataSource(listOf(fakeBtcSymbol))
        val cache = CachedCoinListDataSource(source, ttl = 5.minutes)

        val result = cache.fetchSymbols()

        assertEquals(listOf(fakeBtcSymbol), result)
        assertEquals(1, source.callCount)
    }

    @Test
    fun `fetchSymbols returns cached data without calling source on second call`() = runTest {
        val source = FakeCoinListDataSource(listOf(fakeBtcSymbol, fakeEthSymbol))
        val cache = CachedCoinListDataSource(source, ttl = 5.minutes)

        cache.fetchSymbols()
        val result = cache.fetchSymbols()

        assertEquals(listOf(fakeBtcSymbol, fakeEthSymbol), result)
        assertEquals(1, source.callCount)
    }

    @Test
    fun `fetchSymbols calls source again when cache is invalidated`() = runTest {
        val source = FakeCoinListDataSource(listOf(fakeBtcSymbol))
        val cache = CachedCoinListDataSource(source, ttl = 5.minutes)

        cache.fetchSymbols()
        assertEquals(1, source.callCount)

        cache.invalidateCache()
        cache.fetchSymbols()

        assertEquals(2, source.callCount)
    }

    @Test
    fun `fetchSymbols returns latest data after invalidation`() = runTest {
        val initial = listOf(fakeBtcSymbol)
        val updated = listOf(fakeEthSymbol)
        val source = FakeCoinListDataSource(initial)
        val cache = CachedCoinListDataSource(source, ttl = 5.minutes)

        cache.fetchSymbols()
        source.symbols = updated

        cache.invalidateCache()
        val result = cache.fetchSymbols()

        assertEquals(listOf(fakeEthSymbol), result)
    }

    @Test
    fun `fetchSymbols calls source again when TTL expires`() = runTest {
        val timeProvider = FakeTimeProvider()
        val source = FakeCoinListDataSource(listOf(fakeBtcSymbol, fakeEthSymbol))
        val cache = object : CachedCoinListDataSource(source, ttl = 1.minutes) {
            override fun now(): Long = timeProvider()
        }

        cache.fetchSymbols()
        assertEquals(1, source.callCount)

        timeProvider.advanceBy(61000)

        val result = cache.fetchSymbols()

        assertEquals(listOf(fakeBtcSymbol, fakeEthSymbol), result)
        assertEquals(2, source.callCount)
    }

    @Test
    fun `concurrent calls after TTL expiry only trigger one source fetch`() = runTest {
        val timeProvider = FakeTimeProvider()
        val source = FakeCoinListDataSource(
            symbols = listOf(fakeBtcSymbol, fakeEthSymbol),
            fetchDelay = 10.milliseconds,
        )
        val cache = object : CachedCoinListDataSource(source, ttl = 1.minutes) {
            override fun now(): Long = timeProvider()
        }

        cache.fetchSymbols()
        timeProvider.advanceBy(61000)

        coroutineScope {
            val jobs = (1..10).map {
                launch { cache.fetchSymbols() }
            }
            jobs.forEach { it.join() }
        }

        assertEquals(2, source.callCount)
    }

    @Test
    fun `concurrent calls only trigger one source fetch`() = runTest {
        val source = FakeCoinListDataSource(listOf(fakeBtcSymbol))
        val cache = CachedCoinListDataSource(source, ttl = 5.minutes)

        coroutineScope {
            val jobs = (1..10).map {
                launch { cache.fetchSymbols() }
            }
            jobs.forEach { it.join() }
        }

        assertEquals(1, source.callCount)
    }

    private class FakeCoinListDataSource(
        var symbols: List<CryptoSymbol>,
        private val fetchDelay: Duration = Duration.ZERO,
    ) : CoinListDataSource {
        var callCount = 0

        override suspend fun fetchSymbols(): List<CryptoSymbol> {
            callCount++
            if (fetchDelay > Duration.ZERO) delay(fetchDelay)
            return symbols
        }
    }
}
