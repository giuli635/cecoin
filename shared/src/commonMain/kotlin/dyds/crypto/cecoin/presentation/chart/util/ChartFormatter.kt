package dyds.crypto.cecoin.presentation.chart.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter

fun priceFormatter(): CartesianValueFormatter = CartesianValueFormatter.decimal(
    decimalSeparator = ".",
    thousandsSeparator = ",",
)

fun timeFormatter(): CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
    val ms = value.toLong()
    val sec = ms / 1000
    val m = (sec / 60) % 60
    val h = (sec / 3600) % 24
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
