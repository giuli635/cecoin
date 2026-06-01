package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.TradePriceRepository
import dyds.crypto.cecoin.data.remote.BinanceStreamClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DefaultSymbol = "BTCUSDT"

class DefaultTradePriceRepository(
    private val binanceStreamClient: BinanceStreamClient,
) : TradePriceRepository {
    override fun observeTradePrices(
        symbol: String,
    ): Flow<TradePrice> {
        val normalizedSymbol = symbol.normalizeSymbol()

        return binanceStreamClient
            .tradePrices(normalizedSymbol)
            .map { price -> TradePrice(symbol = normalizedSymbol, price = price) }
    }
}

private fun String.normalizeSymbol(): String =
    trim().uppercase().ifBlank { DefaultSymbol }

