package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.model.TradePrice
import dyds.crypto.cecoin.chart.presentation.model.Granularity
import dyds.crypto.cecoin.core.utils.state.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChartDataControllerTest {

    @Test
    fun `seed with historical data emits Success snapshot`() = runTest {
        val controller = ChartDataController(
            observeTradePricesUseCase = FakeObserveTradePricesUseCase(),
            priceAccumulator = FakePriceAccumulator(historical = fakeTradePricesFromPricePoints(
                PricePoint(0L, 50000.0), PricePoint(60_000L, 51000.0),
            )),
            symbol = "BTCUSDT",
            scope = this,
        )
        controller.startStream()

        val snapshot = controller.chartData.first { it is Fallible.Success }
        val success = snapshot as Fallible.Success
        val data = success.value
        assertEquals(2, data.size)
        assertEquals(51000.0, data.last().price)
        controller.cancel()
    }

    @Test
    fun `trade updates snapshot after seed`() = runTest {
        val fakeUseCase = FakeObserveTradePricesUseCase()
        val controller = ChartDataController(
            observeTradePricesUseCase = fakeUseCase,
            symbol = "BTCUSDT",
            scope = this,
            priceAccumulator = FakePriceAccumulator(),
        )
        controller.startStream()
        fakeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(60_000L, 52000.0)))

        val snapshot = controller.chartData.first {
            it is Fallible.Success && it.value.any { p -> p.price == 52000.0 }
        }
        val data = (snapshot as Fallible.Success).value
        assertTrue(data.any { it.price == 52000.0 })
        controller.cancel()
    }

    @Test
    fun `cancel stops stream processing`() = runTest {
        val fakeUseCase = FakeObserveTradePricesUseCase()
        val controller = ChartDataController(
            observeTradePricesUseCase = fakeUseCase,
            symbol = "BTCUSDT",
            scope = this,
            priceAccumulator = FakePriceAccumulator(),
        )
        controller.startStream()
        controller.cancel()

        fakeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(60_000L, 52000.0)))

        val afterCancel = controller.chartData.value
        val success = afterCancel as Fallible.Success
        assertTrue(success.value.isEmpty())
    }

    @Test
    fun `startStream cancels previous stream when called twice`() = runTest {
        val fakeUseCase = FakeObserveTradePricesUseCase()
        val controller = ChartDataController(
            observeTradePricesUseCase = fakeUseCase,
            symbol = "BTCUSDT",
            scope = this,
            priceAccumulator = FakePriceAccumulator(historical = fakeTradePricesFromPricePoints(PricePoint(0L, 50000.0))),
        )
        controller.startStream()
        controller.startStream()
        fakeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(60_000L, 52000.0)))

        val snapshot = controller.chartData.first {
            it is Fallible.Success && it.value.any { p -> p.price == 52000.0 }
        }
        assertTrue((snapshot as Fallible.Success).value.any { it.price == 52000.0 })
        controller.cancel()
    }

    @Test
    fun `stream emits Failed on error`() = runTest {
        val controller = ChartDataController(
            observeTradePricesUseCase = FakeObserveTradePricesUseCase(exception = RuntimeException("Stream crash")),
            symbol = "BTCUSDT",
            scope = this,
            priceAccumulator = FakePriceAccumulator(),
        )
        controller.startStream()

        val failed = controller.chartData.first { it is Fallible.Failed }
        assertIs<Fallible.Failed>(failed)
        controller.cancel()
    }

    @Test
    fun `cancel when streamJob is null does nothing`() = runTest {
        val controller = ChartDataController(
            observeTradePricesUseCase = FakeObserveTradePricesUseCase(),
            symbol = "BTCUSDT",
            scope = this,
            priceAccumulator = FakePriceAccumulator(),
        )
        controller.cancel()
    }

    @Test
    fun `stream stops retrying on CancellationException`() = runTest {
        val controller = ChartDataController(
            observeTradePricesUseCase = FakeObserveTradePricesUseCase(exception = CancellationException("Cancelled")),
            symbol = "BTCUSDT",
            scope = this,
            priceAccumulator = FakePriceAccumulator(historical = fakeTradePricesFromPricePoints(PricePoint(0L, 50000.0))),
        )
        controller.startStream()

        val snapshot = controller.chartData.first { it is Fallible.Success }
        val data = (snapshot as Fallible.Success).value
        assertEquals(50000.0, data.last().price)
        controller.cancel()
    }
}
