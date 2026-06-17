package dyds.crypto.cecoin.presentation.chart.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import dyds.crypto.cecoin.domain.chart.model.PricePoint
import dyds.crypto.cecoin.presentation.chart.util.VicoChartModelBuilder
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PriceChart(
    points: List<PricePoint>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val chartModelBuilder = remember { VicoChartModelBuilder() }

    LaunchedEffect(points) {
        if (points.isNotEmpty()) {
            chartModelBuilder.buildModel(points, modelProducer)
        }
    }

    Box(modifier = modifier.height(CHART_HEIGHT_DP.dp)) {
        val scrollState = rememberVicoScrollState()
        LaunchedEffect(modelProducer) {
            while (scrollState.maxValue <= 0f) {
                delay(16.milliseconds)
            }
            scrollState.scroll(Scroll.Absolute.End)
            scrollState.scroll(Scroll.Absolute.pixels(scrollState.maxValue - SCROLL_END_PADDING_PX))
        }
        CartesianChartHost(
            rememberCartesianChart(
                rememberChartLayer(),
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                marker = rememberChartMarker(),
                markerController = rememberChartMarkerController(),
            ),
            modelProducer,
            Modifier.fillMaxSize(),
            scrollState = scrollState,
            zoomState = rememberVicoZoomState(
                initialZoom = initialZoom,
                minZoom = minZoom,
            ),
        )
    }
}
