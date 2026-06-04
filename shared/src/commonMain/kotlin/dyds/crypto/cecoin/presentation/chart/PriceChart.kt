package dyds.crypto.cecoin.presentation.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.Renderer

@Composable
fun PriceChart(): Renderer<PricePoints> =
    { value, modifier ->
        val lineColor: Color = MaterialTheme.colorScheme.primary
        val prices = value.prices
        val lastPrice = value.lastPrice

        Text(
            text = "Last: ${lastPrice?.let { (kotlin.math.round(it * 100) / 100).toString() } ?: "—"}",
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

            val points = prices.mapIndexed { i, p ->
                val x = padding + i * stepX
                val y = padding + (((max - p) / range) * h).toFloat()
                Offset(x, y)
            }

            val path = Path()
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cpx = (prev.x + curr.x) / 2f
                path.cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
            }

            val strokePaint = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            drawPath(path = path, color = lineColor, style = strokePaint)

            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, size.height - padding)
                lineTo(points.first().x, size.height - padding)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0f)),
                    startY = padding,
                    endY = size.height - padding,
                ),
            )

            val lastPos = points.last()
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = lastPos)
        }
    }
