package dyds.crypto.cecoin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import dyds.crypto.cecoin.binance.KtorBinanceStreamClient

fun main() = application {
    val http = HttpClient(CIO) {
        install(WebSockets)
    }
    val binanceClient = KtorBinanceStreamClient(http = http)

    Window(
        onCloseRequest = {
            // ensure closing http client
            http.close()
            exitApplication()
        },
        title = "cecoin",
    ) {
        App(client = binanceClient)
    }
}