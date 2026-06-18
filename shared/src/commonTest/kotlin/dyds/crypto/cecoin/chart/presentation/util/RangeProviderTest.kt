package dyds.crypto.cecoin.chart.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RangeProviderTest {
    @Test
    fun `computeChartYRange with valid range returns padded min and max`() {
        val range = computeChartYRange(100.0, 200.0)

        assertTrue(range.min < 100.0)
        assertTrue(range.max > 200.0)
    }

    @Test
    fun `computeChartYRange with equal min and max returns gapped range`() {
        val range = computeChartYRange(100.0, 100.0)

        assertEquals(99.0, range.min)
        assertEquals(101.0, range.max)
    }

    @Test
    fun `computeChartYRange with negative values returns padded range`() {
        val range = computeChartYRange(-200.0, -100.0)

        assertTrue(range.min < -200.0)
        assertTrue(range.max > -100.0)
    }

    @Test
    fun `computeChartXRange with valid range returns padded max`() {
        val range = computeChartXRange(0.0, 3600000.0)

        assertEquals(0.0, range.min)
        assertTrue(range.max > 3600000.0)
    }

    @Test
    fun `computeChartXRange with under minute range uses minute step`() {
        val range = computeChartXRange(0.0, 30_000.0)

        assertEquals(0.0, range.min)
        assertTrue(range.max > 30_000.0)
    }

    @Test
    fun `computeChartXRange with few minute range uses 5-min step`() {
        val range = computeChartXRange(0.0, 300_000.0)

        assertEquals(0.0, range.min)
        assertTrue(range.max > 300_000.0)
    }

    @Test
    fun `computeChartXRange with 20 minute range uses 15-min step`() {
        val range = computeChartXRange(0.0, 1_200_000.0)

        assertEquals(0.0, range.min)
        assertTrue(range.max > 1_200_000.0)
    }

    @Test
    fun `computeChartXRange with 50 minute range uses 30-min step`() {
        val range = computeChartXRange(0.0, 3_000_000.0)

        assertEquals(0.0, range.min)
        assertTrue(range.max > 3_000_000.0)
    }

    @Test
    fun `computeChartXRange with multi-hour range uses hourly step`() {
        val range = computeChartXRange(0.0, 43_200_000.0)

        assertEquals(0.0, range.min)
        assertTrue(range.max > 43_200_000.0)
    }

    @Test
    fun `computeChartXRange with multi-day range uses daily step`() {
        val range = computeChartXRange(0.0, 172_800_000.0)

        assertEquals(0.0, range.min)
        assertTrue(range.max > 172_800_000.0)
    }

    @Test
    fun `computeChartXRange with zero range returns default x range`() {
        val range = computeChartXRange(1000.0, 1000.0)

        assertEquals(1000.0, range.min)
        assertEquals(61000.0, range.max)
    }

    @Test
    fun `niceStep with zero range returns 1`() {
        val result = niceStep(0.0)
        assertEquals(1.0, result)
    }

    @Test
    fun `niceStep with negative range returns 1`() {
        val result = niceStep(-5.0)
        assertEquals(1.0, result)
    }

    @Test
    fun `niceStep with small range returns step 1`() {
        val result = niceStep(1.2)
        assertEquals(1.0, result)
    }

    @Test
    fun `niceStep with medium range returns step 2`() {
        val result = niceStep(2.5)
        assertEquals(2.0, result)
    }

    @Test
    fun `niceStep with larger range returns step 5`() {
        val result = niceStep(5.0)
        assertEquals(5.0, result)
    }

    @Test
    fun `niceStep with big range returns step 10`() {
        val result = niceStep(8.0)
        assertEquals(10.0, result)
    }
}
