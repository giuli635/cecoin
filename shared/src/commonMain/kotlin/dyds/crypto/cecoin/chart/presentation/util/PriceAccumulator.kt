package dyds.crypto.cecoin.chart.presentation.util

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.model.TradePrice
import dyds.crypto.cecoin.chart.presentation.model.Granularity

interface PriceAccumulator {
    fun accumulate(trade: TradePrice)
    fun snapshot(): List<PricePoint>
    fun accumulateAndSnapshot(trade: TradePrice): List<PricePoint> {
        accumulate(trade)
        return snapshot()
    }
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

