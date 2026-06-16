package dyds.crypto.cecoin.presentation.chart.util

import java.time.Instant
import java.time.ZoneId

fun systemTimezoneOffsetMillis(): Long =
    ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds * 1000L
