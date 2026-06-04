package dyds.crypto.cecoin.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

typealias Renderer<T> = @Composable (T, Modifier) -> Unit

fun buildComposableRenderer(inner: @Composable (Modifier) -> Unit): Renderer<Unit> =
    { _, modifier -> inner(modifier) }
