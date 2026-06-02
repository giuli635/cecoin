package dyds.crypto.cecoin.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> OneTimeLoadableComposable(
    inner: Renderer<T>,
    onCancel: () -> Unit,
): Renderer<Flow<Loadable<T>>> = @Composable { value, modifier ->
    val state by value.collectAsState(initial = Loadable.Loading)
    LoadableComposable(inner, onCancel)(state, modifier)
}
