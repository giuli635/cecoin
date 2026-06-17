package dyds.crypto.cecoin.chart.presentation.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import dyds.crypto.cecoin.chart.presentation.util.ChartColors
import dyds.crypto.cecoin.chart.presentation.util.createRangeProvider

@Composable
fun rememberChartLayer() = rememberLineCartesianLayer(
    lineProvider = LineCartesianLayer.LineProvider.series(
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(ChartColors.accent)),
            stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.dp),
            areaFill = LineCartesianLayer.AreaFill.double(
                topFill = Fill(ChartColors.accent.copy(alpha = 0.15f)),
                bottomFill = Fill(ChartColors.accent.copy(alpha = 0f)),
            ),
            interpolator = LineCartesianLayer.Interpolator.catmullRom(),
        ),
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(Color.Transparent)),
            stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        ),
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(Color.Transparent)),
            pointProvider = LineCartesianLayer.PointProvider.single(
                LineCartesianLayer.Point(
                    component = rememberShapeComponent(Fill(ChartColors.accent), CircleShape),
                    size = 12.dp,
                )
            ),
        ),
    ),
    rangeProvider = createRangeProvider(),
)
