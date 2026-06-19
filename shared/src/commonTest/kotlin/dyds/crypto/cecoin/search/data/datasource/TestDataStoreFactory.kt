package dyds.crypto.cecoin.search.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createTestDataStore(): DataStore<Preferences>
