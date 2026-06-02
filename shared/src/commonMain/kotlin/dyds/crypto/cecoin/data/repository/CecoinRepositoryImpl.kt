package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.CecoinRepository
import dyds.crypto.cecoin.data.remote.CoinPriceSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DefaultSymbol = "BTCUSDT"

class CecoinRepositoryImpl(
    private val coinPriceSource: CoinPriceSource,
) : CecoinRepository {

    // TODO: Handle errors and edge cases (e.g., invalid symbols, network issues)
    override fun observeTradePrices(
        symbol: String,
    ): Flow<TradePrice> {
        val normalizedSymbol = symbol.normalizeSymbol()

        return coinPriceSource
            .tradePrices(normalizedSymbol)
            .map { price -> TradePrice(symbol = normalizedSymbol, price = price) }
    }
}

private fun String.normalizeSymbol(): String =
    trim().uppercase().ifBlank { DefaultSymbol }

