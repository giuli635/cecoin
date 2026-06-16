package dyds.crypto.cecoin.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreFavoriteDataSource(
    private val dataStore: DataStore<Preferences>,
) : FavoriteDataSource {
    private val favoritesKey = stringSetPreferencesKey("favorites")

    override val favorites: Flow<Set<String>> = dataStore.data
        .map { preferences -> preferences[favoritesKey] ?: emptySet() }

    override suspend fun toggle(symbol: String) {
        dataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: emptySet()
            preferences[favoritesKey] = if (symbol in current) current - symbol else current + symbol
        }
    }
}
