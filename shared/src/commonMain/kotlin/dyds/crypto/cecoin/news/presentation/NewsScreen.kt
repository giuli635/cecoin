package dyds.crypto.cecoin.news.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import cecoin.shared.generated.resources.Res
import cecoin.shared.generated.resources.news_no_news_available
import cecoin.shared.generated.resources.news_no_news_found_prefix
import cecoin.shared.generated.resources.news_screen_title
import cecoin.shared.generated.resources.news_search_label
import dyds.crypto.cecoin.core.presentation.Renderer
import dyds.crypto.cecoin.news.presentation.component.NewsCard
import dyds.crypto.cecoin.core.presentation.utils.buildAsyncComposable
import dyds.crypto.cecoin.core.domain.state.Loadable
import org.jetbrains.compose.resources.stringResource

@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredNews by viewModel.filteredNews.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.news_screen_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            label = { Text(stringResource(Res.string.news_search_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = filteredNews !is Loadable.Loading,
        )

        buildAsyncComposable(
            viewModel::onCancelLoadNews,
            viewModel::retryLoadNews,
            newsListRenderer(uiState.searchQuery),
        )(filteredNews, Modifier.fillMaxSize())
    }
}

private fun newsListRenderer(
    searchQuery: String,
): Renderer<List<NewsArticle>> = { articles, modifier ->
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(articles) { article ->
            NewsCard(
                article = article,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (articles.isEmpty()) {
            item {
                val message = if (searchQuery.isNotEmpty()) "${stringResource(Res.string.news_no_news_found_prefix)}$searchQuery'"
                else stringResource(Res.string.news_no_news_available)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
