package dyds.crypto.cecoin.core.presentation.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.core.utils.state.Fallible
import dyds.crypto.cecoin.core.utils.state.Loadable
import dyds.crypto.cecoin.core.utils.state.loadable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun <T> ViewModel.launchLoadable(
    state: MutableStateFlow<Loadable<Fallible<T>>>,
    block: suspend () -> Fallible<T>,
): Job = viewModelScope.launch {
    state.value = Loadable.Loading
    state.value = loadable(block)
}
