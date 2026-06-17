package dyds.crypto.cecoin.core.presentation.utils

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
import dyds.crypto.cecoin.core.presentation.Renderer
import dyds.crypto.cecoin.core.utils.CoreStrings

fun <T> buildCancellableComposable(onCancel: () -> Unit, inner: Renderer<T>): Renderer<T> =
    { value, modifier ->
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            inner(value, Modifier)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onCancel) {
                Text(text = CoreStrings.CANCEL)
            }
        }
    }