package dyds.crypto.cecoin.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dyds.crypto.cecoin.di.CecoinDependencyInjector
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getCoinDetailsViewModel
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getGranularityStateHolder
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getNewsViewModel
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getSearchViewModel
import dyds.crypto.cecoin.presentation.chart.ChartScreen
import dyds.crypto.cecoin.presentation.component.TabHeader
import dyds.crypto.cecoin.presentation.news.NewsScreen
import dyds.crypto.cecoin.presentation.search.CoinSearchScreen
import kotlinx.serialization.Serializable

@Serializable
object Home : Tab {
    override val label = "Buscar"
}

@Serializable
object News : Tab {
    override val label = "Noticias"
}

@Serializable
data class Detail(val symbol: String)

private val tabs = listOf(Home, News)

@Composable
private fun TabbedScreen(
    currentTab: Tab,
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TabHeader(
            tabs = tabs,
            selectedTab = currentTab,
            onTabSelected = { tab ->
                navController.navigate(tab) { launchSingleTop = true }
            },
        )
        content()
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(navController, startDestination = Home) {
            homeDestination(navController)
            newsDestination(navController)
            detailDestination(navController)
        }
    }
}

private fun NavGraphBuilder.homeDestination(navController: NavHostController) {
    composable<Home> {
        TabbedScreen(currentTab = Home, navController = navController) {
            CoinSearchScreen(
                viewModel = getSearchViewModel(),
                onCoinSelected = {
                    navController.navigate(Detail(symbol = it))
                }
            )
        }
    }
}

private fun NavGraphBuilder.newsDestination(navController: NavHostController) {
    composable<News> {
        TabbedScreen(currentTab = News, navController = navController) {
            NewsScreen(viewModel = getNewsViewModel())
        }
    }
}

private fun NavGraphBuilder.detailDestination(navController: NavHostController) {
    composable<Detail> { backstackEntry ->
        val detail = backstackEntry.toRoute<Detail>()
        val granularityHolder = getGranularityStateHolder()
        ChartScreen(
            granularityHolder = granularityHolder,
            viewModel = getCoinDetailsViewModel(detail.symbol),
            errorClassifier = CecoinDependencyInjector.errorClassifier,
            onBack = { navController.popBackStack() }
        )
    }
}
