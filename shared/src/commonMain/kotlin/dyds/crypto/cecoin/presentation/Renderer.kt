package dyds.crypto.cecoin.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Renderer<T> {
    @Composable
    fun render(value: T, modifier: Modifier)
}

class ComposableRenderer(
    private val inner: @Composable (Modifier) -> Unit,
): Renderer<Unit> {

    @Composable
    override fun render(value: Unit, modifier: Modifier) {
        inner(modifier)
    }
}
