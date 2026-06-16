package dyds.crypto.cecoin.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getGranularityStateHolder
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getSearchViewModel
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getCoinDetailsViewModel
import dyds.crypto.cecoin.presentation.chart.ChartScreen
import dyds.crypto.cecoin.presentation.search.CoinSearchScreen
import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class Detail(val symbol: String)

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Home) {
        homeDestination(navController)
        detailDestination(navController)
    }
}

private fun NavGraphBuilder.homeDestination(navController: NavHostController) {
    composable<Home> {
        CoinSearchScreen(
            viewModel = getSearchViewModel(),
            onCoinSelected = {
                navController.navigate(Detail(symbol = it))
            }
        )
    }
}

private fun NavGraphBuilder.detailDestination(navController: NavHostController) {
    composable<Detail> { backstackEntry ->
        val detail = backstackEntry.toRoute<Detail>()
        val granularityHolder = getGranularityStateHolder()
        ChartScreen(
            granularityHolder = granularityHolder,
            viewModel = getCoinDetailsViewModel(detail.symbol),
            onBack = { navController.popBackStack() }
        )
    }
}
