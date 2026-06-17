package dyds.crypto.cecoin.presentation.chart.util

import dyds.crypto.cecoin.utils.MILLIS_IN_SECOND_LONG
import java.time.Instant
import java.time.ZoneId

fun systemTimezoneOffsetMillis(): Long =
    ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds * MILLIS_IN_SECOND_LONG
