package dyds.crypto.cecoin.core.presentation.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dyds.crypto.cecoin.core.presentation.Renderer
import dyds.crypto.cecoin.core.utils.StreamStrings
import dyds.crypto.cecoin.core.utils.error.ErrorClassifier
import dyds.crypto.cecoin.core.utils.state.Fallible
import dyds.crypto.cecoin.core.utils.state.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

fun <T> buildAsyncStreamComposable(
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    errorClassifier: ErrorClassifier,
    timeoutMs: Long = 30_000L,
    inner: Renderer<T>,
): Renderer<AsyncResult<Flow<T>>> =
    buildAsyncComposable(
        onCancel = onCancel,
        onRetry = onRetry,
        inner = { flow: Flow<T>, modifier ->
            val asyncResult by produceState(initialValue = Loadable.Loading as AsyncResult<T>) {
                var timedOut = false

                val collector = launch {
                    try {
                        flow.collect { item ->
                            value = Loadable.Loaded(Fallible.Success(item))
                        }
                    } catch (_: CancellationException) {
                        if (!timedOut) {
                            value = Loadable.Cancelled
                        }
                    } catch (e: Exception) {
                        value = Loadable.Loaded(Fallible.Failed(
                            errorClassifier.classify(e, StreamStrings.DATA_FAILED)
                        ))
                    }
                }

                delay(timeoutMs.milliseconds)
                if (value is Loadable.Loading) {
                    timedOut = true
                    collector.cancel()
                    value = Loadable.Loaded(Fallible.Failed(
                        errorClassifier.classify(
                            RuntimeException("Timeout"),
                            StreamStrings.TIMEOUT
                        )
                    ))
                }
            }

            buildAsyncComposable(
                onCancel = onCancel,
                onRetry = onRetry,
                inner = inner,
            )(asyncResult, modifier)
        }
    )
