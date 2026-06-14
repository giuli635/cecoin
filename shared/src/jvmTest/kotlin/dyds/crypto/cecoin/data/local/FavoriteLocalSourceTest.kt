package dyds.crypto.cecoin.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteLocalSourceTest {
    @Test
    fun `initial favorites are loaded from storage`() = runTest {
        val storage = FakeStorage(setOf("BTCUSDT"))
        val source = FavoriteLocalSource(storage)

        val result = source.favorites.first()

        assertEquals(setOf("BTCUSDT"), result)
    }

    @Test
    fun `toggle adds new symbol`() = runTest {
        val storage = FakeStorage()
        val source = FavoriteLocalSource(storage)

        source.toggle("BTCUSDT")

        val result = source.favorites.first()
        assertEquals(setOf("BTCUSDT"), result)
    }

    @Test
    fun `toggle removes existing symbol`() = runTest {
        val storage = FakeStorage(setOf("BTCUSDT"))
        val source = FavoriteLocalSource(storage)

        source.toggle("BTCUSDT")

        val result = source.favorites.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toggle persists to storage on add`() = runTest {
        val storage = FakeStorage()
        val source = FavoriteLocalSource(storage)

        source.toggle("ETHUSDT")

        assertEquals(setOf("ETHUSDT"), storage.saved)
    }

    @Test
    fun `toggle persists to storage on remove`() = runTest {
        val storage = FakeStorage(setOf("ETHUSDT"))
        val source = FavoriteLocalSource(storage)

        source.toggle("ETHUSDT")

        assertTrue(storage.saved.isEmpty())
    }

    @Test
    fun `multiple toggles work correctly`() = runTest {
        val storage = FakeStorage()
        val source = FavoriteLocalSource(storage)

        source.toggle("A")
        source.toggle("B")
        source.toggle("A")

        val result = source.favorites.first()
        assertEquals(setOf("B"), result)
    }

    @Test
    fun `favorites flow emits initial state then toggles`() = runTest {
        val storage = FakeStorage()
        val source = FavoriteLocalSource(storage)

        assertEquals(emptySet<String>(), source.favorites.first())

        source.toggle("A")
        assertEquals(setOf("A"), source.favorites.first())

        source.toggle("B")
        assertEquals(setOf("A", "B"), source.favorites.first())

        source.toggle("A")
        assertEquals(setOf("B"), source.favorites.first())
    }
}

internal class FakeStorage(
    private val initial: Set<String> = emptySet(),
) : FavoriteStorage {
    private var data = initial.toMutableSet()
    var saved: Set<String> = initial

    override fun load(): Set<String> = data.toSet()

    override fun save(favorites: Set<String>) {
        data.clear()
        data.addAll(favorites)
        saved = data.toSet()
    }
}
