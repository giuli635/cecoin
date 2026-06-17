package dyds.crypto.cecoin.chart.presentation.util

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.presentation.model.Granularity

interface PriceAccumulator {
    fun accumulate(point: PricePoint)
    fun snapshot(): List<PricePoint>
    fun accumulateAndSnapshot(point: PricePoint): List<PricePoint> {
        accumulate(point)
        return snapshot()
    }
}

class PriceAccumulatorImpl(private val granularity: Granularity, historical: List<PricePoint>): PriceAccumulator {
    private val points = historical.toMutableList()

    override fun accumulate(point: PricePoint) {
        val timestampBucket = (point.timestamp / granularity.millis) * granularity.millis
        val bucketed = PricePoint(timestampBucket, point.price)
        val last = points.lastOrNull()
        if (last != null && last.timestamp == bucketed.timestamp) {
            points[points.lastIndex] = last.copy(price = point.price)
        } else {
            if (last != null && bucketed.timestamp < last.timestamp) return
            points.add(bucketed)
        }
    }

    override fun snapshot(): List<PricePoint> = points.toList()
}

fun interface PriceAccumulatorFactory {
    operator fun invoke(granularity: Granularity, historical: List<PricePoint>): PriceAccumulator
}

