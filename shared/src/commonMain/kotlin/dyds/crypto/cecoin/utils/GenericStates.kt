package dyds.crypto.cecoin.utils

sealed class Loadable<out T> {
    object Loading : Loadable<Nothing>()
    object Cancelled : Loadable<Nothing>()
    data class Loaded<T>(val value: T) : Loadable<T>()
}

sealed class Fallible<out T> {
    class Failed(val error: AppError) : Fallible<Nothing>()
    data class Success<T>(val value: T) : Fallible<T>()

    inline fun <R> map(transform: (T) -> R): Fallible<R> = when (this) {
        is Success -> Success(transform(value))
        is Failed -> this
    }
}


