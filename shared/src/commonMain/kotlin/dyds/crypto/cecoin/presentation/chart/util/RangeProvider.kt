package dyds.crypto.cecoin.presentation.chart.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import dyds.crypto.cecoin.presentation.chart.model.ChartRange
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

private const val DEFAULT_Y_RANGE_GAP = 1.0
private const val Y_PADDING_FACTOR = 0.06
private const val X_PADDING_FACTOR = 0.05
private const val DEFAULT_X_RANGE_MS = 60_000.0

fun computeChartYRange(dataMin: Double, dataMax: Double): ChartRange {
    if (dataMax <= dataMin) return ChartRange(dataMin - DEFAULT_Y_RANGE_GAP, dataMax + DEFAULT_Y_RANGE_GAP)
    val rawRange = dataMax - dataMin
    val padding = max(rawRange, 1.0) * Y_PADDING_FACTOR
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
    if (rawRange <= 0.0) return ChartRange(dataMin, dataMax + DEFAULT_X_RANGE_MS)
    val step = niceTimeStep(rawRange)
    return ChartRange(
        min = dataMin,
        max = ceil((dataMax + rawRange * X_PADDING_FACTOR) / step) * step,
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

private const val NICE_STEP_THRESHOLD_1 = 1.5
private const val NICE_STEP_THRESHOLD_2 = 3.0
private const val NICE_STEP_THRESHOLD_3 = 7.0
private const val NICE_STEP_VALUE_1 = 1.0
private const val NICE_STEP_VALUE_2 = 2.0
private const val NICE_STEP_VALUE_5 = 5.0
private const val NICE_STEP_VALUE_10 = 10.0

private const val SECONDS_IN_MINUTE = 60
private const val SECONDS_IN_HOUR = 3600
private const val MILLIS_IN_MINUTE = 60_000.0
private const val MILLIS_IN_5_MIN = 300_000.0
private const val MILLIS_IN_15_MIN = 900_000.0
private const val MILLIS_IN_30_MIN = 1_800_000.0
private const val MILLIS_IN_1_HOUR = 3_600_000.0
private const val MILLIS_IN_6_HOURS = 21_600_000.0
private const val MILLIS_IN_SECOND = 1000.0
private const val MILLIS_IN_1_DAY = 86_400_000.0

fun niceStep(range: Double): Double {
    if (range <= 0.0) return NICE_STEP_VALUE_1
    val exponent = floor(log10(range)).toInt()
    val fraction = range / 10.0.pow(exponent)
    val niceFraction = when {
        fraction < NICE_STEP_THRESHOLD_1 -> NICE_STEP_VALUE_1
        fraction < NICE_STEP_THRESHOLD_2 -> NICE_STEP_VALUE_2
        fraction < NICE_STEP_THRESHOLD_3 -> NICE_STEP_VALUE_5
        else -> NICE_STEP_VALUE_10
    }
    return niceFraction * 10.0.pow(exponent)
}

private fun niceTimeStep(rangeMs: Double): Double {
    val rangeSec = rangeMs / MILLIS_IN_SECOND
    return when {
        rangeSec < SECONDS_IN_MINUTE -> MILLIS_IN_MINUTE
        rangeSec < SECONDS_IN_MINUTE * 10 -> MILLIS_IN_5_MIN
        rangeSec < SECONDS_IN_MINUTE * 30 -> MILLIS_IN_15_MIN
        rangeSec < SECONDS_IN_HOUR -> MILLIS_IN_30_MIN
        rangeSec < SECONDS_IN_HOUR * 6 -> MILLIS_IN_1_HOUR
        rangeSec < SECONDS_IN_HOUR * 24 -> MILLIS_IN_6_HOURS
        else -> MILLIS_IN_1_DAY
    }
}
