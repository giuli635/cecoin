package dyds.crypto.cecoin.chart.presentation.util

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.presentation.model.Granularity
import kotlin.test.Test
import kotlin.test.assertEquals

class PriceAccumulatorImplTest {
    @Test
    fun `accumulate into empty list adds bucketed point`() {
        val priceAccumulator = PriceAccumulatorImpl(Granularity.M1, listOf())
        val point = PricePoint(100_000L, 52000.0)

        priceAccumulator.accumulate(point)

        val points = priceAccumulator.snapshot()
        val expectedTimestamp = (100_000L / 60_000L) * 60_000L
        assertEquals(1, points.size)
        assertEquals(expectedTimestamp, points[0].timestamp)
        assertEquals(52000.0, points[0].price)
    }

    @Test
    fun `accumulate in the same time period replaces price`() {
        val priceAccumulator = PriceAccumulatorImpl(
            Granularity.M1,
            listOf(PricePoint(0L, 50000.0))
        )
        val point = PricePoint(30_000L, 52000.0)

        priceAccumulator.accumulate(point)

        val points = priceAccumulator.snapshot()
        assertEquals(1, points.size)
        assertEquals(52000.0, points[0].price)
    }

    @Test
    fun `accumulate with out of order trade is ignored`() {
        val priceAccumulator = PriceAccumulatorImpl(Granularity.M1,
            listOf(PricePoint(60_000L, 52000.0))
        )
        val point = PricePoint(30_000L, 51000.0)

        priceAccumulator.accumulate(point)

        val points = priceAccumulator.snapshot()
        assertEquals(1, points.size)
        assertEquals(52000.0, points[0].price)
    }
}
