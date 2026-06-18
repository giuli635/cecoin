package dyds.crypto.cecoin.core.presentation.utils

import dyds.crypto.cecoin.core.presentation.Renderer
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.Loadable

typealias AsyncResult<T> = Loadable<Fallible<T>>

fun <T> buildAsyncComposable(
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    inner: Renderer<T>
): Renderer<AsyncResult<T>> =
    buildLoadableComposable(onCancel, onRetry, buildFallibleComposable(inner, onCancel, onRetry))
