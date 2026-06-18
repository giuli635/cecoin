package dyds.crypto.cecoin.search.data.repository

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.search.data.FakeCoinListDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SearchRepositoryImplTest {
    private val btcSymbol = fakeBtcSymbol

    @Test
    fun `getAvailableSymbols delegates to list data source`() = runTest {
        val listSource = FakeCoinListDataSource(listOf(btcSymbol))
        val repo = SearchRepositoryImpl(listSource)

        val result = repo.getAvailableSymbols()

        assertEquals(listOf(btcSymbol), result)
    }

    @Test
    fun `getAvailableSymbols returns empty list when source returns empty`() = runTest {
        val listSource = FakeCoinListDataSource(emptyList())
        val repo = SearchRepositoryImpl(listSource)

        val result = repo.getAvailableSymbols()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAvailableSymbols propagates source exception`() = runTest {
        val listSource = FakeCoinListDataSource(exception = RuntimeException("source fail"))
        val repo = SearchRepositoryImpl(listSource)

        assertFailsWith<RuntimeException> {
            repo.getAvailableSymbols()
        }
    }
}
