package dyds.crypto.cecoin.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoriteLocalSource {
    private val _favorites = MutableStateFlow(loadFavoritesFromDisk())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    fun toggle(symbol: String) {
        _favorites.value = if (symbol in _favorites.value) {
            _favorites.value - symbol
        } else {
            _favorites.value + symbol
        }
        saveFavoritesToDisk(_favorites.value)
    }
}
