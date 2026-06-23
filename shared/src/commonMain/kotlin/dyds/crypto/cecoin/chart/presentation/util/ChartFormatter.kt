package dyds.crypto.cecoin.chart.presentation.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import dyds.crypto.cecoin.core.utils.format.pad
import dyds.crypto.cecoin.core.utils.format.priceStr
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.number

fun priceFormatter(): CartesianValueFormatter =
    CartesianValueFormatter { _, value, _ -> priceStr(value) }

fun dateFormatter(): CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
    val instant = Instant.fromEpochMilliseconds(value.toLong())
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    "${local.day.pad(2)}/${local.month.number.pad(2)} ${local.hour.pad(2)}:${local.minute.pad(2)}"
}
