package dyds.crypto.cecoin.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
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
        source.toggle("BTCUSDT")
        val result = source.favorites.first()
        assertEquals(setOf("BTCUSDT"), result)
    }

    @Test
    fun `toggle removes existing symbol`() = runTest {
        val source = createSource()
        source.toggle("BTCUSDT")
        source.toggle("BTCUSDT")
        val result = source.favorites.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `multiple toggles work correctly`() = runTest {
        val source = createSource()
        source.toggle("A")
        source.toggle("B")
        source.toggle("A")
        val result = source.favorites.first()
        assertEquals(setOf("B"), result)
    }

    private fun createSource(file: File? = null): DataStoreFavoriteDataSource {
        val dataStore = PreferenceDataStoreFactory.create {
            file ?: File.createTempFile("favorites_test", ".preferences_pb").also { it.delete() }
        }
        return DataStoreFavoriteDataSource(dataStore)
    }
}
