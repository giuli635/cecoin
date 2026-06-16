package dyds.crypto.cecoin.presentation.utils

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

fun <T> buildAsyncStreamComposable(
    onRetry: () -> Unit,
    inner: Renderer<T>,
): Renderer<AsyncResult<Flow<T>>> =
    buildAsyncComposable(
        onCancel = {},
        onRetry = onRetry,
        inner = { flow: Flow<T>, modifier ->
            val asyncResult by flow
                .map { Loadable.Loaded(Fallible.Success(it)) as AsyncResult<T> }
                .onStart { emit(Loadable.Loading) }
                .catch { e -> emit(Loadable.Loaded(Fallible.Failed(AppError.GenericError(e, "Stream failed")))) }
                .collectAsState(Loadable.Loading)

            buildAsyncComposable(
                onCancel = {},
                onRetry = onRetry,
                inner = inner,
            )(asyncResult, modifier)
        }
    )
