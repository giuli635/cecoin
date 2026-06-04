package dyds.crypto.cecoin.data.remote.coincap

import kotlinx.serialization.Serializable

@Serializable
data class CoinCapMarketsResponse(
    val data: List<CoinCapRemoteMarket>,
)

@Serializable
data class CoinCapRemoteMarket(
    val exchangeId: String,
    val baseId: String,
    val quoteId: String,
    val baseSymbol: String,
    val quoteSymbol: String,
    val priceUsd: String? = null,
    val volumeUsd24Hr: String? = null,
    val percentExchangeVolume: String? = null,
    val tradesCount24Hr: String? = null,
    val updated: Long? = null,
)
