package dyds.crypto.cecoin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dyds.crypto.cecoin.di.CecoinDependencyInjector

fun main() = application {
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