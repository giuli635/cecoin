package dyds.crypto.cecoin.presentation.chart.component

import com.patrykandpatrick.vico.compose.cartesian.Zoom

internal const val CHART_HEIGHT_DP = 480
internal const val SCROLL_END_PADDING_PX = 30f

private const val INITIAL_ZOOM_FACTOR = 0.3f
private const val MIN_ZOOM_FACTOR = 0.1f

internal val initialZoom = Zoom.fixed(INITIAL_ZOOM_FACTOR)
internal val minZoom = Zoom.fixed(MIN_ZOOM_FACTOR)
