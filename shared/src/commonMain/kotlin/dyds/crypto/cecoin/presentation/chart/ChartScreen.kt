package dyds.crypto.cecoin.presentation.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.presentation.chart.component.GranularitySelector
import dyds.crypto.cecoin.presentation.chart.component.PriceChart
import dyds.crypto.cecoin.presentation.chart.util.ChartColors
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable

private const val BACK_BUTTON = "Back"
private const val USD_LABEL = "USD"
private const val RETRY_BUTTON = "Retry"
private const val STREAM_ERROR_RETRY = "Reconnecting..."

@Composable
fun ChartScreen(
    modifier: Modifier = Modifier,
    viewModel: LiveChartViewModel,
    onBack: () -> Unit,
) {
    val asyncLoadState by viewModel.asyncLoadState.collectAsState()
    val streamState by viewModel.streamState.collectAsState()
    val granularity by viewModel.granularity.collectAsState()
    val lastPrice by viewModel.lastPrice.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadPrices() }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = viewModel.symbol,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onBack) { Text(BACK_BUTTON) }
        }

        if (lastPrice > 0.0) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$${String.format("%,.2f", lastPrice)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = ChartColors.accent,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = USD_LABEL,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }

        GranularitySelector(
            selected = granularity,
            onSelect = viewModel::setGranularity,
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                PriceChart(
                    modelProducer = viewModel.modelProducer,
                    modifier = Modifier.padding(8.dp, 4.dp, 4.dp, 4.dp),
                )

                val showSpinner = asyncLoadState is Loadable.Loading
                val showError = asyncLoadState is Loadable.Loaded &&
                    (asyncLoadState as Loadable.Loaded).value is Fallible.Failed

                if (showSpinner) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = ChartColors.accent,
                        )
                    }
                }
                if (showError) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Button(onClick = viewModel::loadPrices) {
                            Text(RETRY_BUTTON)
                        }
                    }
                }
                val streamFailed = streamState is Loadable.Loaded &&
                    (streamState as Loadable.Loaded).value is Fallible.Failed
                if (streamFailed) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = STREAM_ERROR_RETRY,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
