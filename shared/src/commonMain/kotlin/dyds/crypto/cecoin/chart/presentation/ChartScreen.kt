package dyds.crypto.cecoin.chart.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.presentation.component.GranularitySelector
import dyds.crypto.cecoin.chart.presentation.component.PriceChart
import dyds.crypto.cecoin.chart.presentation.model.Granularity
import dyds.crypto.cecoin.chart.presentation.util.ChartColors
import dyds.crypto.cecoin.core.presentation.utils.buildAsyncStreamComposable
import dyds.crypto.cecoin.core.presentation.utils.buildFallibleComposable
import dyds.crypto.cecoin.chart.presentation.ChartStrings
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import dyds.crypto.cecoin.core.utils.format.priceStr

@Composable
private fun ChartContent(data: List<PricePoint>, granularity: Granularity, modifier: Modifier = Modifier) {
    val lastPrice = data.lastOrNull()?.price
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (lastPrice != null) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Text(
                    text = priceStr(lastPrice),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = ChartColors.accent,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = ChartStrings.USD,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
        PriceChart(
            points = data,
            granularity = granularity,
            modifier = Modifier.padding(4.dp),
        )
    }
}

@Composable
fun ChartScreen(
    modifier: Modifier = Modifier,
    granularityHolder: GranularityStateHolder,
    viewModel: ChartScreenViewModel,
    errorClassifier: ErrorClassifier,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val granularity by granularityHolder.granularity.collectAsState()

    LaunchedEffect(granularity) {
        viewModel.load(granularity)
    }

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
                text = viewModel.symbol.symbol,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onBack) { Text(ChartStrings.BACK) }
        }

        GranularitySelector(
            selected = granularity,
            onSelect = granularityHolder::set,
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            buildAsyncStreamComposable(
                onCancel = { viewModel.cancel() },
                onRetry = { viewModel.load(granularity) },
                errorClassifier = errorClassifier,
                inner = buildFallibleComposable(
                    inner = { data: List<PricePoint>, _ ->
                        ChartContent(data, granularity, modifier = Modifier.fillMaxWidth().padding(8.dp))
                    },
                    onCancel = viewModel::cancel,
                    onRetry = { viewModel.load(granularity) },
                ),
            )(state, Modifier.fillMaxSize())
        }
    }
}
