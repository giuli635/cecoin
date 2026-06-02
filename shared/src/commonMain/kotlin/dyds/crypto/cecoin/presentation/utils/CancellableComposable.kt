package dyds.crypto.cecoin.presentation.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.Renderer

class CancellableComposable<T>(
    val inner: Renderer<T>,
    val onCancel: () -> Unit
) : Renderer<T> {

    @Composable
    override fun render(value: T, modifier: Modifier) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            inner.render(value, modifier)

            Spacer(modifier = modifier.height(8.dp))

            OutlinedButton(onClick = onCancel) {
                Text(text = "Cancel")
            }
        }
    }
}