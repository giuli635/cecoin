package dyds.crypto.cecoin.presentation.chart.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ChartFormatterTest {
    @Test
    fun `priceStr formats zero`() {
        assertEquals("$0.00", priceStr(0.0))
    }

    @Test
    fun `priceStr formats integer without commas`() {
        assertEquals("$999.00", priceStr(999.0))
    }

    @Test
    fun `priceStr formats integer with commas`() {
        assertEquals("$1,234.00", priceStr(1234.0))
    }

    @Test
    fun `priceStr formats decimal`() {
        assertEquals("$7.50", priceStr(7.5))
    }

    @Test
    fun `priceStr formats large number`() {
        assertEquals("$1,000,000.00", priceStr(1000000.0))
    }

    @Test
    fun `priceStr formats decimal with commas`() {
        assertEquals("$1,234.50", priceStr(1234.5))
    }
}
