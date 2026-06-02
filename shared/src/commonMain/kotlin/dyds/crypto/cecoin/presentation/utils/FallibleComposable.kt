package dyds.crypto.cecoin.presentation.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible

class FallibleComposable<T>(val inner: Renderer<T>, val onRetry: () -> Unit) : Renderer<Fallible<T>> {
    @Composable
    override fun render(value: Fallible<T>, modifier: Modifier) = when (value) {
        is Fallible.Failed  -> ErrorContent(value.error, modifier)
        is Fallible.Success -> inner.render(value.value, modifier)
    }

    @Composable
    private fun ErrorContent(error: AppError, modifier: Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = error.getMessage(),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}
