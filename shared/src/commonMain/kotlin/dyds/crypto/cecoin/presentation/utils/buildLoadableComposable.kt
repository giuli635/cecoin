package dyds.crypto.cecoin.presentation.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.Renderer
import dyds.crypto.cecoin.presentation.buildComposableRenderer
import dyds.crypto.cecoin.utils.state.Loadable

private const val CANCELLED_MESSAGE = "Carga cancelada"
private const val RETRY_BUTTON = "Reintentar"

fun <T> buildLoadableComposable(onCancel: () -> Unit, onRetry: () -> Unit, inner: Renderer<T>): Renderer<Loadable<T>> =
    { value, modifier -> when (value) {
        is Loadable.Loading -> {
            buildCancellableComposable(
                inner = buildComposableRenderer {
                    Box(
                        Modifier,
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                },
                onCancel = onCancel
            )(Unit, modifier.fillMaxSize())
        }
        is Loadable.Cancelled -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = CANCELLED_MESSAGE,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onRetry) {
                        Text(text = RETRY_BUTTON)
                    }
                }
            }
        }
        is Loadable.Loaded -> inner(value.value, modifier)
    }
}
