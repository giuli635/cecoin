package dyds.crypto.cecoin.search.data.datasource

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.core.utils.fakeEthSymbol
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DataStoreFavoriteDataSourceTest {

    @Test
    fun `initial favorites are empty`() = runTest {
        val source = createSource()
        val result = source.favorites.first()
        assertEquals(emptySet(), result)
    }

    @Test
    fun `toggle adds new symbol`() = runTest {
        val source = createSource()
        source.toggle(fakeBtcSymbol)
        val result = source.favorites.first()
        assertEquals(setOf(fakeBtcSymbol), result)
    }

    @Test
    fun `toggle removes existing symbol`() = runTest {
        val source = createSource()
        source.toggle(fakeBtcSymbol)
        source.toggle(fakeBtcSymbol)
        val result = source.favorites.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `multiple toggles work correctly`() = runTest {
        val source = createSource()
        source.toggle(CryptoSymbol("A"))
        source.toggle(CryptoSymbol("B"))
        source.toggle(CryptoSymbol("A"))
        val result = source.favorites.first()
        assertEquals(setOf(CryptoSymbol("B")), result)
    }

    @Test
    fun `toggle with spaces in symbol works without crashing`() = runTest {
        val source = createSource()
        source.toggle(CryptoSymbol("  BTC  "))
        val result = source.favorites.first()
        assertEquals(setOf(CryptoSymbol("  BTC  ")), result)
    }

    @Test
    fun `toggle with empty symbol does not crash`() = runTest {
        val source = createSource()
        source.toggle(CryptoSymbol(""))
        val result = source.favorites.first()
        assertEquals(setOf(CryptoSymbol("")), result)
    }

    @Test
    fun `concurrent toggles do not lose data`() = runTest {
        val source = createSource()
        coroutineScope {
            launch { source.toggle(fakeBtcSymbol) }
            launch { source.toggle(fakeEthSymbol) }
        }
        val favs = source.favorites.first()
        assertEquals(setOf(fakeBtcSymbol, fakeEthSymbol), favs)
    }

    @Test
    fun `rapid toggling same symbol results in final state being toggled off`() = runTest {
        val source = createSource()
        repeat(4) {
            source.toggle(fakeBtcSymbol)
        }
        val favs = source.favorites.first()
        assertFalse(favs.contains(fakeBtcSymbol))
    }

    private fun createSource(): DataStoreFavoriteDataSource {
        val dataStore = createTestDataStore()
        return DataStoreFavoriteDataSource(dataStore)
    }
}
