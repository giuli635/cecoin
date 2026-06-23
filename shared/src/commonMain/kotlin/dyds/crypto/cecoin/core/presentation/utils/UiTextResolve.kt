package dyds.crypto.cecoin.core.presentation.utils

import androidx.compose.runtime.Composable
import dyds.crypto.cecoin.core.domain.error.UiText
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiText.resolve(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> stringResource(resource, *args)
}
