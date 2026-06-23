package dyds.crypto.cecoin.core.presentation.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import cecoin.shared.generated.resources.Res
import cecoin.shared.generated.resources.stream_data_failed
import cecoin.shared.generated.resources.stream_timeout
import dyds.crypto.cecoin.core.presentation.Renderer
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds

private const val DEFAULT_STREAM_TIMEOUT_MS = 30_000L

fun <T> buildAsyncStreamComposable(
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    errorClassifier: ErrorClassifier,
    timeoutMs: Long = DEFAULT_STREAM_TIMEOUT_MS,
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
                            errorClassifier.classify(e, getString(Res.string.stream_data_failed))
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
                            getString(Res.string.stream_timeout)
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
