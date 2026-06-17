package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.model.TradePrice
import dyds.crypto.cecoin.chart.domain.usecase.FakeGetHistoricalPricesUseCase
import dyds.crypto.cecoin.chart.presentation.model.ChartData
import dyds.crypto.cecoin.chart.presentation.model.Granularity
import dyds.crypto.cecoin.core.utils.state.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChartDataControllerTest {

    @Test
    fun `observe seeds with historical data and emits Success snapshot`() = runTest {
        val controller = ChartDataController(
            getHistoricalPrices = FakeGetHistoricalPricesUseCase(prices = listOf(
                TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
                TradePrice("BTCUSDT", PricePoint(60_000L, 51000.0)),
            )),
            observeTradePrices = FakeObserveTradePricesUseCase(),
            scope = this,
        )

        val fallible = controller.observe("BTCUSDT", Granularity.M1, 200)
        val loaded = assertIs<Fallible.Success<Flow<ChartData>>>(fallible)
        val flow = loaded.value
        val emission = flow.first { it is Fallible.Success }
        val data = assertIs<Fallible.Success<List<PricePoint>>>(emission).value
        assertEquals(2, data.size)
        assertEquals(51000.0, data.last().price)
        controller.cancel()
    }

    @Test
    fun `trade updates snapshot after seed`() = runTest {
        val fakeUseCase = FakeObserveTradePricesUseCase()
        val controller = ChartDataController(
            getHistoricalPrices = FakeGetHistoricalPricesUseCase(),
            observeTradePrices = fakeUseCase,
            scope = this,
        )

        controller.observe("BTCUSDT", Granularity.M1, 200)
        fakeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(60_000L, 52000.0)))

        val snapshot = controller.chartData.first {
            it is Fallible.Success<List<PricePoint>> && it.value.any { p -> p.price == 52000.0 }
        }
        assertIs<Fallible.Success<List<PricePoint>>>(snapshot)
        assertTrue(snapshot.value.any { it.price == 52000.0 })
        controller.cancel()
    }

    @Test
    fun `cancel stops stream processing`() = runTest {
        val fakeUseCase = FakeObserveTradePricesUseCase()
        val controller = ChartDataController(
            getHistoricalPrices = FakeGetHistoricalPricesUseCase(),
            observeTradePrices = fakeUseCase,
            scope = this,
        )

        controller.observe("BTCUSDT", Granularity.M1, 200)
        controller.cancel()

        fakeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(60_000L, 52000.0)))

        val afterCancel = controller.chartData.value
        val success = afterCancel as Fallible.Success<List<PricePoint>>
        assertTrue(success.value.isEmpty())
    }

    @Test
    fun `observe cancels previous observation when called twice`() = runTest {
        val fakeUseCase = FakeObserveTradePricesUseCase()
        val controller = ChartDataController(
            getHistoricalPrices = FakeGetHistoricalPricesUseCase(prices = listOf(
                TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
            )),
            observeTradePrices = fakeUseCase,
            scope = this,
        )

        controller.observe("BTCUSDT", Granularity.M1, 200)
        controller.observe("BTCUSDT", Granularity.M5, 200)
        fakeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(60_000L, 52000.0)))

        val snapshot = controller.chartData.first {
            it is Fallible.Success<List<PricePoint>> && it.value.any { p -> p.price == 52000.0 }
        }
        assertIs<Fallible.Success<List<PricePoint>>>(snapshot)
        assertTrue(snapshot.value.any { it.price == 52000.0 })
        controller.cancel()
    }

    @Test
    fun `stream emits Failed on error`() = runTest {
        val controller = ChartDataController(
            getHistoricalPrices = FakeGetHistoricalPricesUseCase(),
            observeTradePrices = FakeObserveTradePricesUseCase(exception = RuntimeException("Stream crash")),
            scope = this,
        )

        controller.observe("BTCUSDT", Granularity.M1, 200)

        val failed = controller.chartData.first { it is Fallible.Failed }
        assertIs<Fallible.Failed>(failed)
        controller.cancel()
    }

    @Test
    fun `cancel when no active observation does nothing`() = runTest {
        val controller = ChartDataController(
            getHistoricalPrices = FakeGetHistoricalPricesUseCase(),
            observeTradePrices = FakeObserveTradePricesUseCase(),
            scope = this,
        )
        controller.cancel()
    }

    @Test
    fun `stream stops retrying on CancellationException`() = runTest {
        val controller = ChartDataController(
            getHistoricalPrices = FakeGetHistoricalPricesUseCase(prices = listOf(
                TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
            )),
            observeTradePrices = FakeObserveTradePricesUseCase(exception = CancellationException("Cancelled")),
            scope = this,
        )

        controller.observe("BTCUSDT", Granularity.M1, 200)

        val snapshot = controller.chartData.first<Fallible<List<PricePoint>>> { it is Fallible.Success }
        assertIs<Fallible.Success<List<PricePoint>>>(snapshot)
        val data = (snapshot as Fallible.Success<List<PricePoint>>).value
        assertEquals(50000.0, data.last().price)
        controller.cancel()
    }

    @Test
    fun `observe returns Failed when historical fetch fails`() = runTest {
        val controller = ChartDataController(
            getHistoricalPrices = FakeGetHistoricalPricesUseCase(
                exception = RuntimeException("History error"),
            ),
            observeTradePrices = FakeObserveTradePricesUseCase(),
            scope = this,
        )

        val fallible = controller.observe("BTCUSDT", Granularity.M1, 200)
        assertIs<Fallible.Failed>(fallible)
        controller.cancel()
    }
}
