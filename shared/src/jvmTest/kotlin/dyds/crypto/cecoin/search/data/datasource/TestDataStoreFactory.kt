package dyds.crypto.cecoin.search.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File

actual fun createTestDataStore(): DataStore<Preferences> {
    val file = File.createTempFile("favorites_test", ".preferences_pb")
    file.delete()
    file.deleteOnExit()
    return PreferenceDataStoreFactory.create { file }
}
