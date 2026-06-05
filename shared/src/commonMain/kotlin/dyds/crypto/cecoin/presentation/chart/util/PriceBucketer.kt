package dyds.crypto.cecoin.presentation.chart.util

import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.model.toPricePoint
import dyds.crypto.cecoin.presentation.chart.model.Granularity

fun MutableList<PricePoint>.foldTradePrice(trade: TradePrice, granularity: Granularity) {
    val bucketed = trade.toPricePoint(granularity.millis)
    val last = lastOrNull()
    if (last != null && last.timestamp == bucketed.timestamp) {
        this[lastIndex] = last.copy(price = trade.price)
    } else {
        if (last != null && bucketed.timestamp < last.timestamp) return
        add(bucketed)
    }
}
