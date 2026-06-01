package dyds.crypto.cecoin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dyds.crypto.cecoin.di.AppDependencies
import dyds.crypto.cecoin.ui.BinanceLiveChartScreen
import dyds.crypto.cecoin.ui.CoinSearchScreen

sealed interface AppScreen {
    data object Search : AppScreen
    data class Chart(val symbol: String) : AppScreen
}

@Composable
fun App(dependencies: AppDependencies) {
    val currentScreen = remember { mutableStateOf<AppScreen>(AppScreen.Search) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val screen = currentScreen.value) {
                AppScreen.Search -> {
                    CoinSearchScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeContentPadding(),
                        viewModel = dependencies.searchViewModel,
                        onCoinSelected = { symbol ->
                            currentScreen.value = AppScreen.Chart(symbol)
                        },
                    )
                }

                is AppScreen.Chart -> {
                    BinanceLiveChartScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeContentPadding(),
                        viewModel = dependencies.createChartViewModel(screen.symbol),
                        onBackToSearch = {
                            currentScreen.value = AppScreen.Search
                        },
                    )
                }
            }
        }
    }
}

