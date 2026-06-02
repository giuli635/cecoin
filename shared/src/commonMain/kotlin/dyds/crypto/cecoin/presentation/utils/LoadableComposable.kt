package dyds.crypto.cecoin.presentation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.presentation.composableRenderer
import dyds.crypto.cecoin.utils.Loadable

@Composable
fun <T> LoadableComposable(inner: Renderer<T>, onCancel: () -> Unit): Renderer<Loadable<T>> =
    { value, modifier -> when (value) {
        is Loadable.Loading -> {
            CancellableComposable(
                inner = composableRenderer {
                    Box(
                        Modifier,
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                },
                onCancel = onCancel
            )(Unit, modifier.fillMaxSize())
        }
        is Loadable.Loaded -> inner(value.value, modifier)
    }
}
