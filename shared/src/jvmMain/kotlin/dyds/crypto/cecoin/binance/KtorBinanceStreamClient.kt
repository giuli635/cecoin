package dyds.crypto.cecoin.binance

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets

internal fun createBinanceHttpClient(): HttpClient = HttpClient(CIO) {
    install(WebSockets)
}
