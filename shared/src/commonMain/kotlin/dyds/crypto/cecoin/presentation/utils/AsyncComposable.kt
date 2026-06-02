package edu.dyds.movies.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.presentation.utils.FallibleComposable
import dyds.crypto.cecoin.presentation.utils.LoadableComposable
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable

typealias AsyncResult<T> = Loadable<Fallible<T>>

class AsyncComposable<T>(inner: Renderer<T>, onRetry: () -> Unit) : Renderer<AsyncResult<T>> {
    private val composable = LoadableComposable(FallibleComposable(inner, onRetry))

    @Composable
    override fun render(value: AsyncResult<T>, modifier: Modifier) {
        composable.render(value, modifier)
    }
}