package dyds.crypto.cecoin.data.remote.binance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinanceRemoteTicker(
    val symbol: String,
    @SerialName("quoteVolume") val quoteVolume: String,
)
