package dyds.crypto.cecoin.core.presentation.utils

import androidx.compose.runtime.Composable
import cecoin.shared.generated.resources.*
import dyds.crypto.cecoin.core.domain.error.AppError
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val errorTemplates: Map<String, StringResource> = mapOf(
    "error_network" to Res.string.error_network_template,
    "error_cancelled" to Res.string.error_cancelled_template,
    "error_with_message" to Res.string.error_with_message_template,
    "error_unknown" to Res.string.error_unknown_template,
)

private val contextStrings: Map<String, StringResource> = mapOf(
    "load_symbols" to Res.string.context_load_symbols,
    "load_news" to Res.string.context_load_news,
    "load_history" to Res.string.context_load_history,
    "live_stream" to Res.string.context_live_stream,
    "toggle_favorite" to Res.string.context_toggle_favorite,
    "stream_data_failed" to Res.string.stream_data_failed,
    "stream_timeout" to Res.string.stream_timeout,
)

@Composable
fun AppError.toDisplayString(): String {
    val template = errorTemplates[errorKey] ?: return errorKey
    val resolvedArgs = args.map { arg ->
        if (arg is String) contextStrings[arg]?.let { stringResource(it) } ?: arg
        else arg
    }.toTypedArray()
    return stringResource(template, *resolvedArgs)
}
