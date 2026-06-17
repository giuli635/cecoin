package dyds.crypto.cecoin.utils.format

import kotlin.math.pow

fun Int.pad(len: Int) = toString().padStart(len, '0')

fun formatDecimals(v: Double, decimals: Int): String {
    val factor = 10.0.pow(decimals).toLong()
    val whole = v.toLong()
    val dec = ((v - whole) * factor).toInt()
    return "${whole}.${dec.pad(decimals)}"
}

fun formatThousands(v: Long): String {
    val s = v.toString()
    val sb = StringBuilder()
    s.forEachIndexed { i, c ->
        if (i > 0 && (s.length - i) % 3 == 0) sb.append(',')
        sb.append(c)
    }
    return sb.toString()
}

fun priceStr(v: Double, decimals: Int): String {
    val whole = v.toLong()
    val dec = formatDecimals(v, decimals).substringAfter('.')
    return "$${formatThousands(whole)}.$dec"
}

fun priceStr(v: Double) = priceStr(v, 2)
