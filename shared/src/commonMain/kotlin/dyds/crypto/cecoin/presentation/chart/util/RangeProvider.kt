package dyds.crypto.cecoin.presentation.chart.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import dyds.crypto.cecoin.presentation.chart.model.ChartRange
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

fun computeChartYRange(dataMin: Double, dataMax: Double): ChartRange {
    if (dataMax <= dataMin) return ChartRange(dataMin - 1.0, dataMax + 1.0)
    val rawRange = dataMax - dataMin
    val padding = max(rawRange, 1.0) * 0.06
    val paddedMin = dataMin - padding
    val paddedMax = dataMax + padding
    val step = niceStep(paddedMax - paddedMin)
    return ChartRange(
        min = floor(paddedMin / step) * step,
        max = ceil(paddedMax / step) * step,
    )
}

fun computeChartXRange(dataMin: Double, dataMax: Double): ChartRange {
    val rawRange = dataMax - dataMin
    if (rawRange <= 0.0) return ChartRange(dataMin, dataMax + 60_000.0)
    val step = niceTimeStep(rawRange)
    return ChartRange(
        min = dataMin,
        max = ceil((dataMax + rawRange * 0.05) / step) * step,
    )
}

fun createRangeProvider(): CartesianLayerRangeProvider = object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
        computeChartYRange(minY, maxY).min

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
        computeChartYRange(minY, maxY).max

    override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore): Double =
        computeChartXRange(minX, maxX).min

    override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore): Double =
        computeChartXRange(minX, maxX).max
}

fun niceStep(range: Double): Double {
    if (range <= 0.0) return 1.0
    val exponent = floor(log10(range)).toInt()
    val fraction = range / 10.0.pow(exponent)
    val niceFraction = when {
        fraction < 1.5 -> 1.0
        fraction < 3.0 -> 2.0
        fraction < 7.0 -> 5.0
        else -> 10.0
    }
    return niceFraction * 10.0.pow(exponent)
}

private fun niceTimeStep(rangeMs: Double): Double {
    val rangeSec = rangeMs / 1000.0
    return when {
        rangeSec < 60 -> 60_000.0
        rangeSec < 60 * 10 -> 300_000.0
        rangeSec < 60 * 30 -> 900_000.0
        rangeSec < 3600 -> 1_800_000.0
        rangeSec < 3600 * 6 -> 3_600_000.0
        rangeSec < 3600 * 24 -> 21_600_000.0
        else -> 86_400_000.0
    }
}
