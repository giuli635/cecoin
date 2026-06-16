package dyds.crypto.cecoin.presentation

import dyds.crypto.cecoin.domain.FakeTradePriceRepository
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.ChartDataController
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChartDataControllerTest {

    private fun createScope() = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())

    @Test
    fun `seed with historical data emits Success snapshot`() = runTest {
        val tradeFlow = MutableSharedFlow<TradePrice>(extraBufferCapacity = 1)
        val priceSource = FakeTradePriceRepository(tradeFlow = tradeFlow)
        val scope = createScope()
        val controller = ChartDataController(
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            scope = scope,
            historical = listOf(PricePoint(0L, 50000.0), PricePoint(60_000L, 51000.0)),
            granularity = Granularity.M1,
            retryDelayMs = 0,
        )
        controller.startStream()

        val snapshot = controller.chartData.first { it is Fallible.Success }
        val success = snapshot as Fallible.Success
        val data = success.value
        assertEquals(2, data.size)
        assertEquals(51000.0, data.last().price)
        controller.cancel()
        scope.cancel()
    }

    @Test
    fun `trade updates snapshot after seed`() = runTest {
        val tradeFlow = MutableSharedFlow<TradePrice>(extraBufferCapacity = 1)
        val priceSource = FakeTradePriceRepository(
            historical = emptyList(),
            tradeFlow = tradeFlow,
        )
        val scope = createScope()
        val controller = ChartDataController(
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            scope = scope,
            historical = emptyList(),
            granularity = Granularity.M1,
            retryDelayMs = 0,
        )
        controller.startStream()

        tradeFlow.emit(TradePrice("BTCUSDT", PricePoint(60_000L, 52000.0)))

        val snapshot = controller.chartData.first {
            it is Fallible.Success && it.value.any { p -> p.price == 52000.0 }
        }
        val data = (snapshot as Fallible.Success).value
        assertTrue(data.any { it.price == 52000.0 })
        controller.cancel()
        scope.cancel()
    }

    @Test
    fun `cancel stops stream processing`() = runTest {
        val tradeFlow = MutableSharedFlow<TradePrice>(extraBufferCapacity = 1)
        val priceSource = FakeTradePriceRepository(
            historical = emptyList(),
            tradeFlow = tradeFlow,
        )
        val scope = createScope()
        val controller = ChartDataController(
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            scope = scope,
            historical = emptyList(),
            granularity = Granularity.M1,
            retryDelayMs = 0,
        )
        controller.startStream()

        controller.cancel()

        tradeFlow.emit(TradePrice("BTCUSDT", PricePoint(60_000L, 52000.0)))

        val afterCancel = controller.chartData.value
        val success = afterCancel as Fallible.Success
        assertTrue(success.value.isEmpty())
        scope.cancel()
    }

    @Test
    fun `stream emits Failed on error`() = runTest {
        val tradeFlow = flow<Nothing> { throw RuntimeException("Stream crash") }
        val priceSource = FakeTradePriceRepository(
            historical = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeFlow = tradeFlow,
        )
        val scope = createScope()
        val controller = ChartDataController(
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            scope = scope,
            historical = emptyList(),
            granularity = Granularity.M1,
            retryDelayMs = 0,
        )
        controller.startStream()

        val failed = controller.chartData.first { it is Fallible.Failed }
        assertIs<Fallible.Failed>(failed)
        controller.cancel()
        scope.cancel()
    }

    @Test
    fun `stream stops retrying on CancellationException`() = runTest {
        val tradeFlow = flow<Nothing> { throw CancellationException("Cancelled") }
        val priceSource = FakeTradePriceRepository(
            historical = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeFlow = tradeFlow,
        )
        val scope = createScope()
        val controller = ChartDataController(
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            scope = scope,
            historical = listOf(PricePoint(0L, 50000.0)),
            granularity = Granularity.M1,
            retryDelayMs = 0,
        )
        controller.startStream()

        val snapshot = controller.chartData.first { it is Fallible.Success }
        val data = (snapshot as Fallible.Success).value
        assertEquals(50000.0, data.last().price)
        controller.cancel()
        scope.cancel()
    }
}
