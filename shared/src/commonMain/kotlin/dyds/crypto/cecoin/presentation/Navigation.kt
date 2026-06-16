package dyds.crypto.cecoin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getCoinDetailsViewModel
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getNewsViewModel
import dyds.crypto.cecoin.di.CecoinDependencyInjector.getSearchViewModel
import dyds.crypto.cecoin.presentation.chart.ChartScreen
import dyds.crypto.cecoin.presentation.news.NewsScreen
import dyds.crypto.cecoin.presentation.search.CoinSearchScreen
import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
object News

@Serializable
data class Detail(val symbol: String)

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showTabs = currentRoute?.contains("Detail") != true

    Column(modifier = Modifier.fillMaxSize()) {
        if (showTabs) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val isHome = currentRoute?.contains("Home") == true
                val isNews = currentRoute?.contains("News") == true

                TextButton(onClick = {
                    if (!isHome) navController.navigate(Home) { popUpTo(Home) { inclusive = true } }
                }) {
                    Text(
                        text = "Buscar",
                        fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal,
                        color = if (isHome) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TextButton(onClick = {
                    if (!isNews) navController.navigate(News) { popUpTo(Home) }
                }) {
                    Text(
                        text = "Noticias",
                        fontWeight = if (isNews) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNews) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }

        NavHost(navController, startDestination = Home) {
            homeDestination(navController)
            newsDestination()
            detailDestination(navController)
        }
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

private fun NavGraphBuilder.newsDestination() {
    composable<News> {
        NewsScreen(
            viewModel = getNewsViewModel(),
        )
    }
}

private fun NavGraphBuilder.detailDestination(navController: NavHostController) {
    composable<Detail> { backstackEntry ->
        val detail = backstackEntry.toRoute<Detail>()
        ChartScreen(
            viewModel = getCoinDetailsViewModel(detail.symbol),
            onBack = { navController.popBackStack() }
        )
    }
}
