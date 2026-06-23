package dyds.crypto.cecoin.core.domain.error

import org.jetbrains.compose.resources.StringResource

sealed class UiText {
    data class Dynamic(val value: String) : UiText()
    class Resource(val resource: StringResource, vararg val args: Any) : UiText()
}

sealed class AppError {
    abstract val uiText: UiText

    data class NetworkError(override val uiText: UiText) : AppError()
    data class GenericError(val exception: Throwable, override val uiText: UiText) : AppError()
}
