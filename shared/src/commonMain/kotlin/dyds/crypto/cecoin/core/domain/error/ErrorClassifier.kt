package dyds.crypto.cecoin.core.domain.error

import cecoin.shared.generated.resources.Res
import cecoin.shared.generated.resources.error_network_template
import cecoin.shared.generated.resources.error_unknown_template
import kotlinx.coroutines.CancellationException

abstract class ErrorClassifier {
    protected abstract fun isNetworkError(exception: Throwable): Boolean

    fun classify(e: Throwable, context: String): AppError {
        return if (isNetworkError(e)) {
            AppError.NetworkError(
                UiText.Resource(Res.string.error_network_template, context)
            )
        } else {
            AppError.GenericError(e, when {
                e is CancellationException -> UiText.Dynamic(context)
                e.message != null -> UiText.Dynamic("$context: ${e.message}")
                else -> UiText.Resource(Res.string.error_unknown_template, context, e.javaClass.simpleName)
            })
        }
    }
}
