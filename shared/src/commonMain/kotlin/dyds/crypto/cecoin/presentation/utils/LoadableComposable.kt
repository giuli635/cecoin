package dyds.crypto.cecoin.presentation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.utils.Loadable

class LoadableComposable<T>(private val inner: Renderer<T>) : Renderer<Loadable<T>> {
    @Composable
    override fun render(value: Loadable<T>, modifier: Modifier) = when (value) {
        is Loadable.Loading -> { LoadingIndicator(modifier) }
        is Loadable.Loaded  -> inner.render(value.value, modifier)
    }

    @Composable
    private fun LoadingIndicator(modifier: Modifier) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}