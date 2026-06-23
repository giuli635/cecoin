package dyds.crypto.cecoin.core.presentation.utils

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
import cecoin.shared.generated.resources.Res
import cecoin.shared.generated.resources.core_retry
import dyds.crypto.cecoin.core.presentation.Renderer
import dyds.crypto.cecoin.core.presentation.buildComposableRenderer
import dyds.crypto.cecoin.core.domain.error.AppError
import dyds.crypto.cecoin.core.domain.state.Fallible
import org.jetbrains.compose.resources.stringResource


fun <T> buildFallibleComposable(
    inner: Renderer<T>,
    onCancel: () -> Unit,
    onRetry: () -> Unit
): Renderer<Fallible<T>> =
    { value, modifier -> when (value) {
        is Fallible.Success -> inner(value.value, modifier)
        is Fallible.Failed -> ErrorContent(value.error, modifier, onCancel, onRetry)
    }
}

@Composable
private fun ErrorContent(error: AppError, modifier: Modifier, onCancel: () -> Unit, onRetry: () -> Unit) {
    buildCancellableComposable(
        inner = buildComposableRenderer {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error.uiText.resolve(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text(text = stringResource(Res.string.core_retry))
                }
            }
        },
        onCancel = onCancel
    )(Unit, modifier)
}
