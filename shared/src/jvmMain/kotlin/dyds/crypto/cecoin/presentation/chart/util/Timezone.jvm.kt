package dyds.crypto.cecoin.presentation.chart.util

import java.time.Instant
import java.time.ZoneId

actual fun systemTimezoneOffsetMillis(): Long =
    ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds * 1000L
