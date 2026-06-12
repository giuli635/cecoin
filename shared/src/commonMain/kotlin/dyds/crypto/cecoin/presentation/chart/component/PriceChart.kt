package dyds.crypto.cecoin.presentation.chart.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import dyds.crypto.cecoin.presentation.chart.util.ChartColors
import dyds.crypto.cecoin.presentation.chart.util.priceFormatter
import dyds.crypto.cecoin.presentation.chart.util.priceStr
import dyds.crypto.cecoin.presentation.chart.util.timeFormatter

private val startAxisLabel = TextComponent(
    textStyle = TextStyle(
        color = Color(0xFF9E9E9E),
        fontSize = 10.sp,
    ),
    textOverflow = TextOverflow.Clip,
    margins = Insets(0.dp),
    padding = Insets(2.dp, 0.dp),
)

private const val INITIAL_ZOOM_FACTOR = 0.5f
private const val MIN_ZOOM_FACTOR = 0.1f
private const val CHART_HEIGHT_DP = 480

private val initialZoom = Zoom.fixed(INITIAL_ZOOM_FACTOR)
private val minZoom = Zoom.fixed(MIN_ZOOM_FACTOR)

@Composable
fun PriceChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.height(CHART_HEIGHT_DP.dp)) {
        val markerIndicator = rememberLineComponent(
            fill = Fill(ChartColors.accent),
            thickness = 10.dp,
            shape = CircleShape,
        )
        CartesianChartHost(
            rememberCartesianChart(
                rememberChartLayer(),
                startAxis = VerticalAxis.rememberStart(
                    guideline = rememberLineComponent(
                        fill = Fill(Color(0xFF9E9E9E)),
                        thickness = 0.5.dp,
                    ),
                    valueFormatter = priceFormatter(),
                    label = startAxisLabel,
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    guideline = rememberLineComponent(
                        fill = Fill(Color(0xFF9E9E9E)),
                        thickness = 0.5.dp,
                    ),
                    valueFormatter = timeFormatter(),
                    label = startAxisLabel,
                ),
                marker = rememberDefaultCartesianMarker(
                    label = rememberTextComponent(
                        style = TextStyle(
                            color = ChartColors.markerText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        lineCount = 2,
                        padding = Insets(12.dp, 6.dp),
                        background = rememberShapeComponent(
                            shape = RoundedCornerShape(8.dp),
                            fill = Fill(ChartColors.markerBackground),
                        ),
                    ),
                    valueFormatter = remember {
                        DefaultCartesianMarker.ValueFormatter { _, targets ->
                            targets
                                .filterIsInstance<LineCartesianLayerMarkerTarget>()
                                .flatMap { it.points }
                                .filter { it.entry.seriesIndex == 0 }
                                .joinToString("\n") { priceStr(it.entry.y) }
                        }
                    },
                    guideline = rememberLineComponent(
                        fill = Fill(Color(0xFF9E9E9E)),
                        thickness = 0.5.dp,
                    ),
                    indicator = { markerIndicator },
                ),
                markerController = CartesianMarkerController.rememberToggleOnTap(),
            ),
            modelProducer,
            Modifier.fillMaxSize(),
            scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End),
            zoomState = rememberVicoZoomState(
                initialZoom = initialZoom,
                minZoom = minZoom,
            ),
        )
    }
}
