package dyds.crypto.cecoin.chart.presentation.model

import dyds.crypto.cecoin.core.utils.MILLIS_IN_1_DAY
import dyds.crypto.cecoin.core.utils.MILLIS_IN_1_HOUR
import dyds.crypto.cecoin.core.utils.MILLIS_IN_5_MIN
import dyds.crypto.cecoin.core.utils.MILLIS_IN_6_HOURS
import dyds.crypto.cecoin.core.utils.MILLIS_IN_15_MIN
import dyds.crypto.cecoin.core.utils.MILLIS_IN_30_MIN
import dyds.crypto.cecoin.core.utils.MILLIS_IN_MINUTE

enum class Granularity(
    val label: String,
    val interval: String,
    val millis: Long,
) {
    M1("1m", "1m", MILLIS_IN_MINUTE),
    M3("3m", "3m", 3 * MILLIS_IN_MINUTE),
    M5("5m", "5m", MILLIS_IN_5_MIN),
    M15("15m", "15m", MILLIS_IN_15_MIN),
    M30("30m", "30m", MILLIS_IN_30_MIN),
    H1("1h", "1h", MILLIS_IN_1_HOUR),
    H2("2h", "2h", 2 * MILLIS_IN_1_HOUR),
    H4("4h", "4h", 4 * MILLIS_IN_1_HOUR),
    H6("6h", "6h", MILLIS_IN_6_HOURS),
    H12("12h", "12h", 12 * MILLIS_IN_1_HOUR),
    D1("1d", "1d", MILLIS_IN_1_DAY),
}
