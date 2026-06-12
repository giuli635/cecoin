package dyds.crypto.cecoin.utils

sealed class Loadable<out T> {
    object Loading : Loadable<Nothing>()
    data class Loaded<T>(val value: T) : Loadable<T>()
}

sealed class Fallible<out T> {
    class Failed(val error: AppError) : Fallible<Nothing>()
    data class Success<T>(val value: T) : Fallible<T>()
}


