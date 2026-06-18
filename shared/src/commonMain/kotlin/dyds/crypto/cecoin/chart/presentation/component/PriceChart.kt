package dyds.crypto.cecoin.chart.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.presentation.util.VicoChartModelBuilder
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

private const val SCROLL_LAYOUT_TIMEOUT_MS = 2000L
private const val LAST_POINT_VIEWPORT_BIAS = 0.5f

@Composable
fun PriceChart(
    points: List<PricePoint>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val chartModelBuilder = remember { VicoChartModelBuilder() }
    val activeDatasetFirstTimestamp = remember { mutableStateOf<Long?>(null) }

    Box(modifier = modifier.height(CHART_HEIGHT_DP.dp)) {
        val scrollState = rememberVicoScrollState()

        LaunchedEffect(points) {
            if (points.isNotEmpty()) {
                val currentFirstTimestamp = points.first().timestamp
                val isNewDataset = currentFirstTimestamp != activeDatasetFirstTimestamp.value
                activeDatasetFirstTimestamp.value = currentFirstTimestamp
                chartModelBuilder.buildModel(points, modelProducer)
                if (isNewDataset) {
                    val maxValueBeforeNewModel = scrollState.maxValue
                    withTimeoutOrNull(SCROLL_LAYOUT_TIMEOUT_MS.milliseconds) {
                        snapshotFlow { scrollState.maxValue }
                            .filter { it > 0f && it != maxValueBeforeNewModel }
                            .first()
                    }
                    scrollState.scroll(
                        Scroll.Absolute.x(
                            x = points.last().timestamp.toDouble(),
                            bias = LAST_POINT_VIEWPORT_BIAS,
                        )
                    )
                }
            }
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
