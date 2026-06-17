package dyds.crypto.cecoin.search.data.datasource

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataStoreFavoriteDataSourceTest {
    private var tempFile: File? = null

    @AfterTest
    fun cleanup() {
        tempFile?.delete()
    }

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

    private fun createSource(): DataStoreFavoriteDataSource {
        val file = File.createTempFile("favorites_test", ".preferences_pb")
        file.delete()
        tempFile = file
        val dataStore = PreferenceDataStoreFactory.create { file }
        return DataStoreFavoriteDataSource(dataStore)
    }
}
