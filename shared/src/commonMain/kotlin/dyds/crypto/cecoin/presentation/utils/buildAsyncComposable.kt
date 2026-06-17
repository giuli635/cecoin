package dyds.crypto.cecoin.presentation.utils

import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.state.Loadable

typealias AsyncResult<T> = Loadable<Fallible<T>>

fun <T> buildAsyncComposable(
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    inner: Renderer<T>
): Renderer<AsyncResult<T>> =
    buildLoadableComposable(onCancel, onRetry, buildFallibleComposable(inner, onCancel, onRetry))
