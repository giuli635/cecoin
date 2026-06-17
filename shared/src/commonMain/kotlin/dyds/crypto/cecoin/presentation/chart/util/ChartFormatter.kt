package dyds.crypto.cecoin.presentation.chart.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import dyds.crypto.cecoin.utils.format.priceStr
import dyds.crypto.cecoin.utils.format.pad

private const val MILLIS_IN_SECOND = 1000
private const val SECONDS_IN_MINUTE = 60
private const val SECONDS_IN_HOUR = 3600
private const val HOURS_IN_DAY = 24

fun priceFormatter(): CartesianValueFormatter =
    CartesianValueFormatter { _, value, _ -> priceStr(value) }

fun timeFormatter(): CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
    val ms = value.toLong() + systemTimezoneOffsetMillis()
    val sec = ms / MILLIS_IN_SECOND
    val m = (sec / SECONDS_IN_MINUTE) % SECONDS_IN_MINUTE
    val h = (sec / SECONDS_IN_HOUR) % HOURS_IN_DAY
    "${h.toInt().pad(2)}:${m.toInt().pad(2)}"
}