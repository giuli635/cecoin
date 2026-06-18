package dyds.crypto.cecoin.chart.presentation.component

import com.patrykandpatrick.vico.compose.cartesian.Zoom

internal const val CHART_HEIGHT_DP = 480

private const val INITIAL_ZOOM_FACTOR = 0.3f
private const val MIN_ZOOM_FACTOR = 0.1f

internal val initialZoom = Zoom.fixed(INITIAL_ZOOM_FACTOR)
internal val minZoom = Zoom.fixed(MIN_ZOOM_FACTOR)

