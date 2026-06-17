package dyds.crypto.cecoin.data.search.repository

import dyds.crypto.cecoin.data.search.FakeCoinListDataSource
import dyds.crypto.cecoin.domain.search.model.CryptoSymbol
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchRepositoryImplTest {
    private val btcSymbol = CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING")

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
}
