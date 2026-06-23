package dyds.crypto.cecoin.search.presentation.util

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBnbSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.core.utils.fakeEthSymbol
import dyds.crypto.cecoin.search.presentation.FilterMode
import kotlin.test.Test
import kotlin.test.assertEquals

class SymbolFilterTest {

    private val allSymbols = listOf(fakeBtcSymbol, fakeEthSymbol, fakeBnbSymbol)

    @Test
    fun `all mode with empty query returns all symbols`() {
        val result = allSymbols.filterBy("", FilterMode.ALL, emptySet())
        assertEquals(allSymbols, result)
    }

    @Test
    fun `all mode with query filters case insensitive`() {
        val result = allSymbols.filterBy("btc", FilterMode.ALL, emptySet())
        assertEquals(listOf(fakeBtcSymbol), result)
    }

    @Test
    fun `all mode with query matching none returns empty`() {
        val result = allSymbols.filterBy("xrp", FilterMode.ALL, emptySet())
        assertEquals(emptyList(), result)
    }

    @Test
    fun `all mode with partial query matches by contains`() {
        val result = allSymbols.filterBy("USDT", FilterMode.ALL, emptySet())
        assertEquals(allSymbols, result)
    }

    @Test
    fun `favorites mode with empty query returns only favorites`() {
        val favorites = setOf(fakeBtcSymbol, fakeBnbSymbol)
        val result = allSymbols.filterBy("", FilterMode.FAVORITES, favorites)
        assertEquals(listOf(fakeBnbSymbol, fakeBtcSymbol), result.sorted())
    }

    @Test
    fun `favorites mode with query filters favorites`() {
        val favorites = setOf(fakeBtcSymbol, fakeEthSymbol, fakeBnbSymbol)
        val result = allSymbols.filterBy("ETH", FilterMode.FAVORITES, favorites)
        assertEquals(listOf(fakeEthSymbol), result)
    }

    @Test
    fun `favorites mode with no favorites match returns empty`() {
        val favorites = setOf(fakeBtcSymbol)
        val result = allSymbols.filterBy("ETH", FilterMode.FAVORITES, favorites)
        assertEquals(emptyList(), result)
    }

    @Test
    fun `favorites mode with symbol not in favorites returns empty`() {
        val favorites = setOf(CryptoSymbol("XRPUSDT"))
        val result = allSymbols.filterBy("", FilterMode.FAVORITES, favorites)
        assertEquals(emptyList(), result)
    }

    @Test
    fun `empty list returns empty regardless of mode`() {
        val result = emptyList<CryptoSymbol>().filterBy("", FilterMode.ALL, emptySet())
        assertEquals(emptyList(), result)
    }

}
