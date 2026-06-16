package dyds.crypto.cecoin.presentation.chart.util

import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import kotlin.test.Test
import kotlin.test.assertEquals

class PriceBucketerTest {
    @Test
    fun `fold into empty list adds bucketed point`() {
        val points = mutableListOf<PricePoint>()
        val trade = TradePrice("BTCUSDT", PricePoint(100_000L, 52000.0))
        points.foldTradePrice(trade, Granularity.M1)

        val expectedTimestamp = (100_000L / 60_000L) * 60_000L
        assertEquals(1, points.size)
        assertEquals(expectedTimestamp, points[0].timestamp)
        assertEquals(52000.0, points[0].price)
    }

    @Test
    fun `fold with same bucket replaces price`() {
        val points = mutableListOf(PricePoint(0L, 50000.0))
        val trade = TradePrice("BTCUSDT", PricePoint(30_000L, 52000.0))
        points.foldTradePrice(trade, Granularity.M1)

        assertEquals(1, points.size)
        assertEquals(52000.0, points[0].price)
    }

    @Test
    fun `fold with out of order trade is ignored`() {
        val points = mutableListOf(PricePoint(60_000L, 52000.0))
        val trade = TradePrice("BTCUSDT", PricePoint(30_000L, 51000.0))
        points.foldTradePrice(trade, Granularity.M1)

        assertEquals(1, points.size)
        assertEquals(52000.0, points[0].price)
    }
}
