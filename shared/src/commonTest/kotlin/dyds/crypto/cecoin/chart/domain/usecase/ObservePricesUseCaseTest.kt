package dyds.crypto.cecoin.chart.domain.usecase

import dyds.crypto.cecoin.chart.domain.FakePriceRepository
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.core.domain.error.fakeErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class ObservePricesUseCaseTest {
    private val classifier = fakeErrorClassifier()

    @Test
    fun `invoke returns flow from repository`() = runTest {
        val expected = PricePoint(1000L, 50000.0)
        val repo = FakePriceRepository(tradeFlow = flowOf(expected))
        val useCase = ObservePricesUseCaseImpl(repo, classifier)

        val result = useCase(fakeBtcSymbol)

        val fallible = result.first()
        val success = fallible as Fallible.Success
        assertEquals(expected, success.value)
    }

    @Test
    fun `invoke emits multiple trades`() = runTest {
        val p1 = PricePoint(1000L, 50000.0)
        val p2 = PricePoint(2000L, 51000.0)
        val repo = FakePriceRepository(tradeFlow = flow { emit(p1); emit(p2) })
        val useCase = ObservePricesUseCaseImpl(repo, classifier)

        val results = useCase(fakeBtcSymbol).take(2).toList()

        assertEquals(2, results.size)
        val s1 = assertIs<Fallible.Success<PricePoint>>(results[0])
        assertEquals(50000.0, s1.value.price)
        val s2 = assertIs<Fallible.Success<PricePoint>>(results[1])
        assertEquals(51000.0, s2.value.price)
    }

    @Test
    fun `invoke exhausts retries and stops`() = runTest {
        val repo = FakePriceRepository(tradeFlow = flow { throw RuntimeException("stream fail") })
        val useCase = ObservePricesUseCaseImpl(repo, classifier, retryDelayMs = 1L, maxRetries = 3)

        val results = useCase(fakeBtcSymbol).toList()

        assertEquals(4, results.size)
        results.forEach { assertIs<Fallible.Failed>(it) }
    }

    @Test
    fun `invoke emits nothing when repository flow never emits`() = runTest {
        val repo = FakePriceRepository(tradeFlow = flow { delay(Long.MAX_VALUE) })
        val useCase = ObservePricesUseCaseImpl(repo, classifier)

        val result = withTimeoutOrNull(100.milliseconds) {
            useCase(fakeBtcSymbol).firstOrNull()
        }

        assertNull(result)
    }

    @Test
    fun `invoke handles many emissions without accumulation issues`() = runTest {
        val points = (1..100).map { PricePoint(it.toLong(), 50000.0 + it) }
        val repo = FakePriceRepository(tradeFlow = flow { points.forEach { emit(it) } })
        val useCase = ObservePricesUseCaseImpl(repo, classifier)

        val results = useCase(fakeBtcSymbol).take(100).toList()

        assertEquals(100, results.size)
        results.forEach { assertIs<Fallible.Success<PricePoint>>(it) }
    }
}
