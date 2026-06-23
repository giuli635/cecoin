package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.presentation.model.Granularity
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class GranularityStateHolderTest {

    @Test
    fun `initializes with M1`() {
        val holder = GranularityStateHolder()
        assertEquals(Granularity.M1, holder.granularity.value)
    }

    @Test
    fun `set updates value`() = runTest {
        val holder = GranularityStateHolder()
        holder.set(Granularity.M5)
        assertEquals(Granularity.M5, holder.granularity.value)
    }

    @Test
    fun `set same value does not emit`() = runTest {
        val holder = GranularityStateHolder()
        holder.set(Granularity.M1)
        val next = withTimeoutOrNull(100.milliseconds) {
            holder.granularity.drop(1).first()
        }
        assertNull(next)
    }
}
