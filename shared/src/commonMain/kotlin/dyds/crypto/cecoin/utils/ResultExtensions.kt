package dyds.crypto.cecoin.utils

import kotlinx.coroutines.CancellationException

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
