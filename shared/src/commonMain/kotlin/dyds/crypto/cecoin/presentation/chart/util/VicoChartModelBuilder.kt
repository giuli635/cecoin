package dyds.crypto.cecoin.presentation.chart.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import dyds.crypto.cecoin.domain.model.PricePoint

class VicoChartModelBuilder : ChartModelBuilder {
    override suspend fun buildModel(points: List<PricePoint>, modelProducer: CartesianChartModelProducer) {
        if (points.isEmpty()) return
        val last = points.last()
        val x = points.map { it.timestamp.toDouble() }
        val y = points.map { it.price }
        val lastX = last.timestamp.toDouble()
        modelProducer.runTransaction {
            lineModel {
                series(x = x, y = y)
                series(x = listOf(x.first(), lastX), y = listOf(last.price, last.price))
                series(x = listOf(lastX), y = listOf(last.price))
            }
        }
    }
}
