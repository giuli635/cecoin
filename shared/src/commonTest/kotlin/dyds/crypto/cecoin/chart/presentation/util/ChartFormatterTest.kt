package dyds.crypto.cecoin.chart.presentation.util

import dyds.crypto.cecoin.core.utils.format.priceStr
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartFormatterTest {
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

}
