package dyds.crypto.cecoin.binance

actual fun createBinanceStreamClient(): BinanceStreamClient =
    KtorBinanceStreamClient(http = createBinanceHttpClient())
