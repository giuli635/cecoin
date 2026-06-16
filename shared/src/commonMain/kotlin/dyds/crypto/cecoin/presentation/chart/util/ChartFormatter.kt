package dyds.crypto.cecoin.presentation.chart.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter

private const val MILLIS_IN_SECOND = 1000
private const val SECONDS_IN_MINUTE = 60
private const val SECONDS_IN_HOUR = 3600
private const val HOURS_IN_DAY = 24

fun priceFormatter(): CartesianValueFormatter = CartesianValueFormatter.decimal(
    decimalSeparator = ".",
    thousandsSeparator = ",",
)

fun timeFormatter(): CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
    val ms = value.toLong() + systemTimezoneOffsetMillis()
    val sec = ms / MILLIS_IN_SECOND
    val m = (sec / SECONDS_IN_MINUTE) % SECONDS_IN_MINUTE
    val h = (sec / SECONDS_IN_HOUR) % HOURS_IN_DAY
    "${h.toInt().pad(2)}:${m.toInt().pad(2)}"
}

private fun Int.pad(len: Int) = toString().padStart(len, '0')

fun priceStr(v: Double): String {
    val whole = v.toLong()
    val cents = ((v - whole) * 100).toInt()
    val wStr = whole.toString()
    val sb = StringBuilder()
    wStr.forEachIndexed { i, c ->
        if (i > 0 && (wStr.length - i) % 3 == 0) sb.append(',')
        sb.append(c)
    }
    return "$$sb.${cents.toString().padStart(2, '0')}"
}
