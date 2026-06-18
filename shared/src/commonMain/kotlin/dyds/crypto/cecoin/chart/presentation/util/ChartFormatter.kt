package dyds.crypto.cecoin.chart.presentation.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import dyds.crypto.cecoin.core.utils.format.pad
import dyds.crypto.cecoin.core.utils.format.priceStr

fun priceFormatter(): CartesianValueFormatter =
    CartesianValueFormatter { _, value, _ -> priceStr(value) }

fun dateFormatter(): CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
    val zoned = java.time.Instant.ofEpochMilli(value.toLong())
        .atZone(java.time.ZoneId.systemDefault())
    val day = zoned.dayOfMonth.pad(2)
    val mon = zoned.monthValue.pad(2)
    val h = zoned.hour.pad(2)
    val m = zoned.minute.pad(2)
    "$day/$mon $h:$m"
}