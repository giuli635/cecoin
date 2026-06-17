package dyds.crypto.cecoin.search.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreFavoriteDataSource(
    private val dataStore: DataStore<Preferences>,
) : FavoriteDataSource {
    private val favoritesKey = stringSetPreferencesKey("favorites")

    override val favorites: Flow<Set<CryptoSymbol>> = dataStore.data
        .map { preferences -> (preferences[favoritesKey] ?: emptySet()).map { CryptoSymbol(it) }.toSet() }

    override suspend fun toggle(symbol: CryptoSymbol) {
        dataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: emptySet()
            preferences[favoritesKey] = if (symbol.symbol in current) current - symbol.symbol else current + symbol.symbol
        }
    }
}
