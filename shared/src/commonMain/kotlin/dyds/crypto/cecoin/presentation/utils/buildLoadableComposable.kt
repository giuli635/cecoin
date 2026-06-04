package dyds.crypto.cecoin.presentation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.presentation.buildComposableRenderer
import dyds.crypto.cecoin.utils.Loadable

fun <T> buildLoadableComposable(onCancel: () -> Unit, inner: Renderer<T>): Renderer<Loadable<T>> =
    { value, modifier -> when (value) {
        is Loadable.Loading -> {
            buildCancellableComposable(
                inner = buildComposableRenderer {
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
