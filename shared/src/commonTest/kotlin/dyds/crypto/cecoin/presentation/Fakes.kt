package dyds.crypto.cecoin.presentation

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.presentation.chart.util.ChartModelBuilder

internal class FakeChartModelBuilder : ChartModelBuilder {
    var called = false
    var lastPoints: List<PricePoint> = emptyList()
    var lastProducer: CartesianChartModelProducer? = null

    override suspend fun buildModel(
        points: List<PricePoint>,
        modelProducer: CartesianChartModelProducer,
    ) {
        called = true
        lastPoints = points
        lastProducer = modelProducer
    }
}
