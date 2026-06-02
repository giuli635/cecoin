package dyds.crypto.cecoin.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.flow.Flow

class OneTimeLoadableComposable<T>(
    private val inner: Renderer<T>,
    private val onCancel: () -> Unit
): Renderer<Flow<Loadable<T>>> {
    @Composable
    override fun render(
        value: Flow<Loadable<T>>,
        modifier: Modifier
    ) {
        val state by value.collectAsState(initial = Loadable.Loading)

        LoadableComposable(inner, onCancel).render(state, modifier)
    }
}
