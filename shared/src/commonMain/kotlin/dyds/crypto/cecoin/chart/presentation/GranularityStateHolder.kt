package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.presentation.model.Granularity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GranularityStateHolder {
    private val _granularity = MutableStateFlow(Granularity.M1)
    val granularity: StateFlow<Granularity> = _granularity.asStateFlow()

    fun set(g: Granularity) {
        if (_granularity.value == g) return
        _granularity.value = g
    }
}
