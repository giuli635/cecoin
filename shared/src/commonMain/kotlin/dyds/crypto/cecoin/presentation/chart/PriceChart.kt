package dyds.crypto.cecoin.presentation.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.Renderer

class PriceChart: Renderer<PricePoints> {

    @Composable
    override fun render(value: PricePoints, modifier: Modifier) {
        val lineColor: Color = MaterialTheme.colorScheme.primary
        val prices = value.prices

        Text(
            text = "Last: ${value.lastPrice?.toString() ?: "—"}",
            fontWeight = FontWeight.Medium,
        )

        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            if (prices.size < 2) return@Canvas

            val min = prices.minOrNull() ?: return@Canvas
            val max = prices.maxOrNull() ?: return@Canvas
            val range = (max - min).takeIf { it > 0.0 } ?: 1.0

            val padding = 12.dp.toPx()
            val w = (size.width - 2f * padding).coerceAtLeast(1f)
            val h = (size.height - 2f * padding).coerceAtLeast(1f)

            val stepX = w / (prices.size - 1)

            val path = Path()
            prices.forEachIndexed { i, p ->
                val x = padding + i * stepX
                val y = padding + (((max - p) / range) * h).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )

            val lastX = padding + (prices.size - 1) * stepX
            val lastY = padding + (((max - prices.last()) / range) * h).toFloat()
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(lastX, lastY),
            )
        }
    }
}
