package dyds.crypto.cecoin.data.remote.coincap

val SYMBOL_TO_ASSET_ID = mapOf(
    "BTC" to "bitcoin",
    "ETH" to "ethereum",
    "USDT" to "tether",
    "BNB" to "binance-coin",
    "SOL" to "solana",
    "XRP" to "xrp",
    "ADA" to "cardano",
    "DOGE" to "dogecoin",
    "DOT" to "polkadot",
    "MATIC" to "matic-network",
)

fun symbolToAssetId(symbol: String): String {
    val baseAsset = symbol.trim().uppercase().let { s ->
        s.removeSuffix("USDT").removeSuffix("USD").removeSuffix("BTC")
    }
    return SYMBOL_TO_ASSET_ID[baseAsset] ?: baseAsset.lowercase()
}
