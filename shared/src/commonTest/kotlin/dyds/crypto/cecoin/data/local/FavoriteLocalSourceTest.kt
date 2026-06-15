package dyds.crypto.cecoin.data.local

import dyds.crypto.cecoin.data.FakeFavoriteStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteLocalSourceTest {
    @Test
    fun `initial favorites are loaded from storage`() = runTest {
        val storage = FakeFavoriteStorage(setOf("BTCUSDT"))
        val source = FavoriteLocalSource(storage)

        val result = source.favorites.first()

        assertEquals(setOf("BTCUSDT"), result)
    }

    @Test
    fun `toggle adds new symbol`() = runTest {
        val storage = FakeFavoriteStorage()
        val source = FavoriteLocalSource(storage)

        source.toggle("BTCUSDT")

        val result = source.favorites.first()
        assertEquals(setOf("BTCUSDT"), result)
    }

    @Test
    fun `toggle removes existing symbol`() = runTest {
        val storage = FakeFavoriteStorage(setOf("BTCUSDT"))
        val source = FavoriteLocalSource(storage)

        source.toggle("BTCUSDT")

        val result = source.favorites.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toggle persists to storage on add`() = runTest {
        val storage = FakeFavoriteStorage()
        val source = FavoriteLocalSource(storage)

        source.toggle("ETHUSDT")

        assertEquals(setOf("ETHUSDT"), storage.saved)
    }

    @Test
    fun `toggle persists to storage on remove`() = runTest {
        val storage = FakeFavoriteStorage(setOf("ETHUSDT"))
        val source = FavoriteLocalSource(storage)

        source.toggle("ETHUSDT")

        assertTrue(storage.saved.isEmpty())
    }

    @Test
    fun `multiple toggles work correctly`() = runTest {
        val storage = FakeFavoriteStorage()
        val source = FavoriteLocalSource(storage)

        source.toggle("A")
        source.toggle("B")
        source.toggle("A")

        val result = source.favorites.first()
        assertEquals(setOf("B"), result)
    }

}
