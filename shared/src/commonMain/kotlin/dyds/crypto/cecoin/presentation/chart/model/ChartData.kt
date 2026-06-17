package dyds.crypto.cecoin.presentation.chart.model

import dyds.crypto.cecoin.domain.chart.model.PricePoint
import dyds.crypto.cecoin.utils.state.Fallible

typealias ChartData = Fallible<List<PricePoint>>
