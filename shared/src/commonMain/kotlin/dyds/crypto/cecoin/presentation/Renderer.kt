package dyds.crypto.cecoin.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Renderer<T> {
    @Composable
    fun render(value: T, modifier: Modifier)
}
