package dyds.crypto.cecoin.chart.presentation.model

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.utils.state.Fallible

typealias ChartData = Fallible<List<PricePoint>>
