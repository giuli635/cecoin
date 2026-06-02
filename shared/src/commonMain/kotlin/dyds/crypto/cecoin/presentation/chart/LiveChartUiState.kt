package dyds.crypto.cecoin.presentation.chart

import dyds.crypto.cecoin.utils.Fallible

typealias ChartState = Fallible<PricePoints>

data class PricePoints(
    val prices: List<Double> = emptyList(),
) {
    val lastPrice: Double? get() = prices.lastOrNull()
}
