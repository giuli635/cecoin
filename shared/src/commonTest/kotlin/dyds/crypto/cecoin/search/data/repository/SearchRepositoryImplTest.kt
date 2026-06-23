package dyds.crypto.cecoin.search.data.repository

import dyds.crypto.cecoin.core.data.caching.CachedDataSource
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.core.utils.fakeEthSymbol
import dyds.crypto.cecoin.search.data.FakeCoinListDataSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class SearchRepositoryImplTest {
    private val btcSymbol = fakeBtcSymbol

    @Test
    fun `getAvailableSymbols delegates to list data source on first call`() = runTest {
        val listSource = FakeCoinListDataSource(listOf(btcSymbol))
        val repo = SearchRepositoryImpl(listSource, CachedDataSource(listSource::fetchSymbols, 5.minutes))

        val result = repo.getAvailableSymbols()

        assertEquals(listOf(btcSymbol), result)
        assertEquals(1, listSource.callCount)
    }

    @Test
    fun `getAvailableSymbols returns cached data without calling source on second call`() = runTest {
        val listSource = FakeCoinListDataSource(listOf(btcSymbol, fakeEthSymbol))
        val repo = SearchRepositoryImpl(listSource, CachedDataSource(listSource::fetchSymbols, 5.minutes))

        repo.getAvailableSymbols()
        val result = repo.getAvailableSymbols()

        assertEquals(listOf(btcSymbol, fakeEthSymbol), result)
        assertEquals(1, listSource.callCount)
    }

    @Test
    fun `getAvailableSymbols calls source again when cache is invalidated`() = runTest {
        val listSource = FakeCoinListDataSource(listOf(btcSymbol))
        val repo = SearchRepositoryImpl(listSource, CachedDataSource(listSource::fetchSymbols, 5.minutes))

        repo.getAvailableSymbols()
        assertEquals(1, listSource.callCount)

        repo.invalidateCache()
        repo.getAvailableSymbols()

        assertEquals(2, listSource.callCount)
    }

    @Test
    fun `getAvailableSymbols returns latest data after invalidation`() = runTest {
        val initial = listOf(btcSymbol)
        val updated = listOf(fakeEthSymbol)
        val listSource = FakeCoinListDataSource(initial)
        val repo = SearchRepositoryImpl(listSource, CachedDataSource(listSource::fetchSymbols, 5.minutes))

        repo.getAvailableSymbols()
        listSource.symbols = updated

        repo.invalidateCache()
        val result = repo.getAvailableSymbols()

        assertEquals(listOf(fakeEthSymbol), result)
    }

    @Test
    fun `concurrent calls only trigger one source fetch`() = runTest {
        val listSource = FakeCoinListDataSource(listOf(btcSymbol))
        val repo = SearchRepositoryImpl(listSource, CachedDataSource(listSource::fetchSymbols, 5.minutes))

        coroutineScope {
            val jobs = (1..10).map {
                launch { repo.getAvailableSymbols() }
            }
            jobs.forEach { it.join() }
        }

        assertEquals(1, listSource.callCount)
    }

    @Test
    fun `getAvailableSymbols returns empty list when source returns empty`() = runTest {
        val listSource = FakeCoinListDataSource(emptyList())
        val repo = SearchRepositoryImpl(listSource, CachedDataSource(listSource::fetchSymbols, 5.minutes))

        val result = repo.getAvailableSymbols()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAvailableSymbols propagates source exception`() = runTest {
        val listSource = FakeCoinListDataSource(exception = RuntimeException("source fail"))
        val repo = SearchRepositoryImpl(listSource, CachedDataSource(listSource::fetchSymbols, 5.minutes))

        assertFailsWith<RuntimeException> {
            repo.getAvailableSymbols()
        }
    }

    @Test
    fun `invalidateCache on empty cache does not crash`() = runTest {
        val listSource = FakeCoinListDataSource(listOf(btcSymbol))
        val repo = SearchRepositoryImpl(listSource, CachedDataSource(listSource::fetchSymbols, 5.minutes))

        repo.invalidateCache()
        val result = repo.getAvailableSymbols()

        assertEquals(listOf(btcSymbol), result)
    }
}
