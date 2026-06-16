package dyds.crypto.cecoin.presentation.chart.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import dyds.crypto.cecoin.presentation.chart.util.priceFormatter
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

@Composable
fun rememberStartAxis() = VerticalAxis.rememberStart(
    guideline = rememberLineComponent(
        fill = Fill(Color(0xFF9E9E9E)),
        thickness = 0.5.dp,
    ),
    valueFormatter = priceFormatter(),
    label = startAxisLabel,
)

@Composable
fun rememberBottomAxis() = HorizontalAxis.rememberBottom(
    guideline = rememberLineComponent(
        fill = Fill(Color(0xFF9E9E9E)),
        thickness = 0.5.dp,
    ),
    valueFormatter = timeFormatter(),
    label = startAxisLabel,
)
