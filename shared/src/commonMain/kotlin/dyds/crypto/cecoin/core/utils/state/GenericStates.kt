package dyds.crypto.cecoin.core.utils.state

import dyds.crypto.cecoin.core.utils.error.AppError
import dyds.crypto.cecoin.core.utils.error.ErrorClassifier
import kotlinx.coroutines.CancellationException

sealed class Loadable<out T> {
    object Loading : Loadable<Nothing>()
    object Cancelled : Loadable<Nothing>()
    data class Loaded<T>(val value: T) : Loadable<T>()

    inline fun <R> map(transform: (T) -> R): Loadable<R> = when (this) {
        is Loading -> Loading
        is Cancelled -> Cancelled
        is Loaded -> Loaded(transform(value))
    }
}

sealed class Fallible<out T> {
    class Failed(val error: AppError) : Fallible<Nothing>()
    data class Success<T>(val value: T) : Fallible<T>()

    inline fun <R> map(transform: (T) -> R): Fallible<R> = when (this) {
        is Success -> Success(transform(value))
        is Failed -> this
    }
}


suspend inline fun <T> runCatchingCancellable(crossinline block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

fun <T> Result<T>.toFallible(errorClassifier: ErrorClassifier, message: String): Fallible<T> {
    return fold(
        onSuccess = { Fallible.Success(it) },
        onFailure = { Fallible.Failed(errorClassifier.classify(it, message)) }
    )
}

suspend fun <T> loadable(block: suspend () -> Fallible<T>): Loadable<Fallible<T>> = try {
    Loadable.Loaded(block())
} catch (_: CancellationException) {
    Loadable.Cancelled
}