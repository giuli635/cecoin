package dyds.crypto.cecoin.chart.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import dyds.crypto.cecoin.chart.presentation.util.dateFormatter
import dyds.crypto.cecoin.chart.presentation.util.priceFormatter

@Composable
private fun startAxisLabel(): TextComponent {
    val color = MaterialTheme.colorScheme.outlineVariant
    return remember(color) {
        TextComponent(
            textStyle = TextStyle(
                color = color,
                fontSize = 10.sp,
            ),
            textOverflow = TextOverflow.Clip,
            margins = Insets(0.dp),
            padding = Insets(2.dp, 0.dp),
        )
    }
}

@Composable
fun rememberStartAxis() = VerticalAxis.rememberStart(
    guideline = rememberLineComponent(
        fill = Fill(MaterialTheme.colorScheme.outlineVariant),
        thickness = 0.5.dp,
    ),
    valueFormatter = priceFormatter(),
    label = startAxisLabel(),
)

@Composable
fun rememberBottomAxis() = HorizontalAxis.rememberBottom(
    guideline = rememberLineComponent(
        fill = Fill(MaterialTheme.colorScheme.outlineVariant),
        thickness = 0.5.dp,
    ),
    valueFormatter = dateFormatter(),
    label = startAxisLabel(),
)
