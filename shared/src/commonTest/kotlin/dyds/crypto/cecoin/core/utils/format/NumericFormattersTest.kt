package dyds.crypto.cecoin.core.utils.format

import kotlin.test.Test
import kotlin.test.assertEquals

class NumericFormattersTest {

    @Test
    fun `pad with len greater than digits adds leading zeros`() {
        assertEquals("0042", 42.pad(4))
    }

    @Test
    fun `pad with len equal to digits returns same`() {
        assertEquals("42", 42.pad(2))
    }

    @Test
    fun `pad with len less than digits returns full digits`() {
        assertEquals("12345", 12345.pad(3))
    }

    @Test
    fun `pad zero`() {
        assertEquals("0000", 0.pad(4))
    }

    @Test
    fun `pad with len zero returns original string`() {
        assertEquals("0", 0.pad(0))
    }

    @Test
    fun `formatDecimals with normal value`() {
        assertEquals("123.4560", formatDecimals(123.456, 4))
    }

    @Test
    fun `formatDecimals with zero decimals`() {
        assertEquals("123.0", formatDecimals(123.0, 1))
    }

    @Test
    fun `formatDecimals with integer value`() {
        assertEquals("100.00", formatDecimals(100.0, 2))
    }

    @Test
    fun `formatDecimals with small decimal`() {
        assertEquals("0.05", formatDecimals(0.05, 2))
    }

    @Test
    fun `formatDecimals with negative value`() {
        assertEquals("-5.00", formatDecimals(-5.0, 2))
    }

    @Test
    fun `formatDecimals with negative non-integer`() {
        assertEquals("-5.30", formatDecimals(-5.3, 2))
    }

    @Test
    fun `formatDecimals with value less than one`() {
        assertEquals("0.0010", formatDecimals(0.001, 4))
    }

    @Test
    fun `formatDecimals with floating point rounding`() {
        formatDecimals(0.1 + 0.2, 2)
    }

    @Test
    fun `formatDecimals with all nines`() {
        assertEquals("9.99", formatDecimals(9.99, 2))
    }

    @Test
    fun `formatDecimals large value`() {
        assertEquals("999999.50", formatDecimals(999999.5, 2))
    }

    @Test
    fun `formatThousands with normal value`() {
        assertEquals("1,234,567", formatThousands(1234567L))
    }

    @Test
    fun `formatThousands with less than thousand`() {
        assertEquals("999", formatThousands(999L))
    }

    @Test
    fun `formatThousands with exactly thousand`() {
        assertEquals("1,000", formatThousands(1000L))
    }

    @Test
    fun `formatThousands with zero`() {
        assertEquals("0", formatThousands(0L))
    }

    @Test
    fun `formatThousands with very large value`() {
        assertEquals("12,345,678,901", formatThousands(12345678901L))
    }

    @Test
    fun `formatThousands with four digits`() {
        assertEquals("5,000", formatThousands(5000L))
    }

    @Test
    fun `formatThousands with negative value`() {
        assertEquals("-1,234", formatThousands(-1234L))
    }

    @Test
    fun `priceStr with default decimals`() {
        assertEquals($$"$1,234.50", priceStr(1234.5))
    }

    @Test
    fun `priceStr with custom decimals`() {
        assertEquals($$"$1,234.5670", priceStr(1234.567, 4))
    }

    @Test
    fun `priceStr with zero`() {
        assertEquals($$"$0.00", priceStr(0.0))
    }

    @Test
    fun `priceStr with value less than one`() {
        assertEquals($$"$0.50", priceStr(0.5))
    }

    @Test
    fun `priceStr with negative value`() {
        assertEquals($$"-$100.00", priceStr(-100.0))
    }

    @Test
    fun `priceStr with very large value`() {
        assertEquals("$1,000,000.00", priceStr(1000000.0))
    }

    @Test
    fun `priceStr with zero decimals`() {
        assertEquals("$1,234.0", priceStr(1234.0, 1))
    }
}
