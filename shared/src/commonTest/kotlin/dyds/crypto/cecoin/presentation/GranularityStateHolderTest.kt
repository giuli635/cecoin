package dyds.crypto.cecoin.presentation

import dyds.crypto.cecoin.presentation.chart.GranularityStateHolder
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import kotlin.test.Test
import kotlin.test.assertEquals

class GranularityStateHolderTest {

    @Test
    fun `initializes with M1`() {
        val holder = GranularityStateHolder()
        assertEquals(Granularity.M1, holder.granularity.value)
    }

    @Test
    fun `set updates value`() {
        val holder = GranularityStateHolder()
        holder.set(Granularity.M5)
        assertEquals(Granularity.M5, holder.granularity.value)
    }

    @Test
    fun `set same value does not emit`() {
        val holder = GranularityStateHolder()
        holder.set(Granularity.M1)
        assertEquals(Granularity.M1, holder.granularity.value)
    }
}
