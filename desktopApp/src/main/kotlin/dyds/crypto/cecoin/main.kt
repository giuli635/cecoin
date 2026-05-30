package dyds.crypto.cecoin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "cecoin",
    ) {
        App()
    }
}