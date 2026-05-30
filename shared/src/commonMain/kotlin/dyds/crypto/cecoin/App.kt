package dyds.crypto.cecoin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dyds.crypto.cecoin.ui.BinanceLiveChartScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BinanceLiveChartScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .safeContentPadding(),
            )
        }
    }
}
