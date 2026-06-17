package dyds.crypto.cecoin.chart.presentation.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import dyds.crypto.cecoin.chart.domain.model.PricePoint

interface ChartModelBuilder {
    suspend fun buildModel(points: List<PricePoint>, modelProducer: CartesianChartModelProducer)
}
