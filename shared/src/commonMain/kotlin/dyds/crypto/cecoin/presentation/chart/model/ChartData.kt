package dyds.crypto.cecoin.presentation.chart.model

import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.utils.Fallible

typealias ChartData = Fallible<List<PricePoint>>
