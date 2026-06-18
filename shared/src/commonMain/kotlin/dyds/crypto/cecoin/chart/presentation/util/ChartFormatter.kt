package dyds.crypto.cecoin.chart.presentation.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import dyds.crypto.cecoin.core.utils.HOURS_IN_DAY
import dyds.crypto.cecoin.core.utils.MILLIS_IN_SECOND
import dyds.crypto.cecoin.core.utils.SECONDS_IN_HOUR
import dyds.crypto.cecoin.core.utils.SECONDS_IN_MINUTE
import dyds.crypto.cecoin.core.utils.format.pad
import dyds.crypto.cecoin.core.utils.format.priceStr

fun priceFormatter(): CartesianValueFormatter =
    CartesianValueFormatter { _, value, _ -> priceStr(value) }

fun timeFormatter(): CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
    val ms = value.toLong() + systemTimezoneOffsetMillis()
    val sec = ms / MILLIS_IN_SECOND
    val m = (sec / SECONDS_IN_MINUTE) % SECONDS_IN_MINUTE
    val h = (sec / SECONDS_IN_HOUR) % HOURS_IN_DAY
    "${h.toInt().pad(2)}:${m.toInt().pad(2)}"
}

fun dateFormatter(): CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
    val zoned = java.time.Instant.ofEpochMilli(value.toLong())
        .atZone(java.time.ZoneId.systemDefault())
    val day = zoned.dayOfMonth.pad(2)
    val mon = zoned.monthValue.pad(2)
    val h = zoned.hour.pad(2)
    val m = zoned.minute.pad(2)
    "$day/$mon $h:$m"
}