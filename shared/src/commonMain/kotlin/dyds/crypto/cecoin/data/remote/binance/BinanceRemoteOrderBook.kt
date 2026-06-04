package dyds.crypto.cecoin.data.remote.binance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinanceRemoteOrderBook(
    @SerialName("lastUpdateId") val lastUpdateId: Long,
    val bids: List<List<String>>,
    val asks: List<List<String>>,
)
