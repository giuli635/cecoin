package dyds.crypto.cecoin.core.presentation.utils

import dyds.crypto.cecoin.core.presentation.Renderer
import dyds.crypto.cecoin.core.utils.state.Fallible
import dyds.crypto.cecoin.core.utils.state.Loadable

typealias AsyncResult<T> = Loadable<Fallible<T>>

fun <T> buildAsyncComposable(
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    inner: Renderer<T>
): Renderer<AsyncResult<T>> =
    buildLoadableComposable(onCancel, onRetry, buildFallibleComposable(inner, onCancel, onRetry))
