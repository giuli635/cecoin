package dyds.crypto.cecoin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dyds.crypto.cecoin.core.di.CecoinDependencyInjector
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import java.io.File

fun main() {
    val dataStore = PreferenceDataStoreFactory.create {
        File(System.getProperty("user.home"), ".cecoin/favorites.preferences_pb")
            .also { it.parentFile.mkdirs() }
    }

    val errorClassifier = object : ErrorClassifier() {
        override fun isNetworkError(exception: Throwable): Boolean =
            exception is java.io.IOException ||
                exception.javaClass.name.contains("UnresolvedAddressException") ||
                exception.javaClass.name.contains("NoRouteToHostException") ||
                exception.message?.contains("No route to host") == true ||
                exception.message?.contains("Network is unreachable") == true
    }

    CecoinDependencyInjector.configure(dataStore, errorClassifier)

    application {
        Window(
            onCloseRequest = {
                CecoinDependencyInjector.dispose()
                exitApplication()
            },
            title = "cecoin",
        ) {
            App()
        }
    }
}