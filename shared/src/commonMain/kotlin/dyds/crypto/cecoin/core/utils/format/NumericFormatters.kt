package dyds.crypto.cecoin.core.utils.format

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

fun Int.pad(len: Int) = toString().padStart(len, '0')

fun formatDecimals(v: Double, decimals: Int): String {
    val factor = 10.0.pow(decimals).toLong()
    val whole = v.toLong()
    val dec = abs(((v - whole) * factor).roundToLong()).toInt()
    return "${whole}.${dec.pad(decimals)}"
}

fun formatThousands(v: Long): String {
    val negative = v < 0
    val s = if (negative) (-v).toString() else v.toString()
    val sb = StringBuilder()
    s.forEachIndexed { i, c ->
        if (i > 0 && (s.length - i) % 3 == 0) sb.append(',')
        sb.append(c)
    }
    return if (negative) "-$sb" else sb.toString()
}

fun priceStr(v: Double, decimals: Int): String {
    val prefix = if (v < 0) "-" else ""
    val absV = abs(v)
    val whole = absV.toLong()
    val dec = formatDecimals(absV, decimals).substringAfter('.')
    return "${prefix}$${formatThousands(whole)}.$dec"
}

fun priceStr(v: Double) = priceStr(v, 2)
