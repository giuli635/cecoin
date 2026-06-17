package dyds.crypto.cecoin.chart.domain.usecase

import dyds.crypto.cecoin.chart.domain.FakePriceRepository
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.core.utils.error.fakeErrorClassifier
import dyds.crypto.cecoin.core.utils.state.Fallible
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetHistoricalPricesUseCaseTest {
    private val classifier = fakeErrorClassifier()

    @Test
    fun `invoke delegates to repository with correct params`() = runTest {
        val expected = listOf(PricePoint(1000L, 50000.0))
        val repo = FakePriceRepository(historical = expected)
        val useCase = GetHistoricalPricesUseCaseImpl(repo, classifier)

        val result = useCase(fakeBtcSymbol, "5m", 100)

        val success = assertIs<Fallible.Success<List<PricePoint>>>(result)
        assertEquals(expected, success.value)
    }

    @Test
    fun `invoke uses default interval and limit`() = runTest {
        val expected = listOf(PricePoint(2000L, 3000.0))
        val repo = FakePriceRepository(historical = expected)
        val useCase = GetHistoricalPricesUseCaseImpl(repo, classifier)

        val result = useCase(CryptoSymbol("ETHUSDT"))

        val success = assertIs<Fallible.Success<List<PricePoint>>>(result)
        assertEquals(expected, success.value)
    }
}
