package dyds.crypto.cecoin.presentation.chart.util

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import dyds.crypto.cecoin.domain.model.PricePoint

interface ChartModelBuilder {
    suspend fun buildModel(points: List<PricePoint>, modelProducer: CartesianChartModelProducer)
}
