package dyds.crypto.cecoin.presentation.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.utils.error.ErrorClassifier
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.state.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun <T> ViewModel.launchAsync(
    state: MutableStateFlow<AsyncResult<T>>,
    errorClassifier: ErrorClassifier? = null,
    errorMessage: String? = null,
    block: suspend () -> Fallible<T>,
): Job = viewModelScope.launch {
    try {
        state.value = Loadable.Loaded(block())
    } catch (_: CancellationException) {
        state.value = Loadable.Cancelled
    } catch (e: Exception) {
        if (errorClassifier != null) {
            state.value = Loadable.Loaded(Fallible.Failed(
                errorClassifier.classify(e, errorMessage ?: e.message ?: "Unexpected error")
            ))
        } else {
            throw e
        }
    }
}

fun <T> ViewModel.launchLoadable(
    state: MutableStateFlow<Loadable<Fallible<T>>>,
    block: suspend () -> Fallible<T>,
): Job = viewModelScope.launch {
    state.value = Loadable.Loading
    try {
        state.value = Loadable.Loaded(block())
    } catch (_: CancellationException) {
        state.value = Loadable.Cancelled
    }
}
