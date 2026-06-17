package dyds.crypto.cecoin.presentation.chart.util

import dyds.crypto.cecoin.domain.chart.model.PricePoint
import dyds.crypto.cecoin.domain.chart.model.TradePrice
import dyds.crypto.cecoin.presentation.chart.model.Granularity

interface PriceAccumulator {
    fun accumulate(trade: TradePrice)
    fun snapshot(): List<PricePoint>
}

class PriceAccumulatorImpl(private val granularity: Granularity, historical: List<TradePrice>): PriceAccumulator {
    private val points = mutableListOf<PricePoint>()

    init {
        historical.forEach { accumulate(it) }
    }

    override fun accumulate(trade: TradePrice) {
        val timestampBucket = (trade.timestamp / granularity.millis) * granularity.millis
        val bucketed = PricePoint(timestampBucket, trade.price)
        val last = points.lastOrNull()
        if (last != null && last.timestamp == bucketed.timestamp) {
            points[points.lastIndex] = last.copy(price = trade.price)
        } else {
            if (last != null && bucketed.timestamp < last.timestamp) return
            points.add(bucketed)
        }
    }

    override fun snapshot(): List<PricePoint> = points.toList()
}

