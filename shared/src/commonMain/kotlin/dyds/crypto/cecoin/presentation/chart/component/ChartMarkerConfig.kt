package dyds.crypto.cecoin.presentation.chart.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import dyds.crypto.cecoin.presentation.chart.util.ChartColors
import dyds.crypto.cecoin.utils.format.priceStr

@Composable
fun rememberChartMarker(): DefaultCartesianMarker {
    val markerIndicator = rememberLineComponent(
        fill = Fill(ChartColors.accent),
        thickness = 10.dp,
        shape = CircleShape,
    )
    return rememberDefaultCartesianMarker(
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
            fill = Fill(MaterialTheme.colorScheme.outlineVariant),
            thickness = 0.5.dp,
        ),
        indicator = { markerIndicator },
    )
}

@Composable
fun rememberChartMarkerController() = CartesianMarkerController.rememberToggleOnTap()
