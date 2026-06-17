package dyds.crypto.cecoin.search.domain.model

data class CryptoSymbol(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val status: String
)

