package dyds.crypto.cecoin.presentation.utils

import androidx.compose.runtime.Composable
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable

typealias AsyncResult<T> = Loadable<Fallible<T>>

@Composable
fun <T> AsyncComposable(
    inner: Renderer<T>,
    onCancel: () -> Unit,
    onRetry: () -> Unit
): Renderer<AsyncResult<T>> =
    LoadableComposable(FallibleComposable(inner, onCancel, onRetry), onCancel)
