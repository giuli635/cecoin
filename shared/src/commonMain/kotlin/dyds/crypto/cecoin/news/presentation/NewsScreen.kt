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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.core.presentation.Renderer
import dyds.crypto.cecoin.news.presentation.component.NewsCard
import dyds.crypto.cecoin.core.presentation.utils.buildAsyncComposable
import dyds.crypto.cecoin.core.utils.NewsStrings
import dyds.crypto.cecoin.core.utils.state.Loadable

@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val asyncNews by viewModel.asyncNews.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = NewsStrings.TITLE,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            label = { Text(NewsStrings.SEARCH_LABEL) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = asyncNews !is Loadable.Loading,
        )

        buildAsyncComposable(
            viewModel::onCancelLoadNews,
            viewModel::retryLoadNews,
            newsListRenderer(uiState.searchQuery, viewModel),
        )(asyncNews, Modifier.fillMaxSize())
    }
}

private fun newsListRenderer(
    searchQuery: String,
    viewModel: NewsViewModel,
): Renderer<List<NewsArticle>> = { articles, modifier ->
    val filtered = if (searchQuery.isEmpty()) articles
    else articles.filter { it.title.contains(searchQuery, ignoreCase = true) }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filtered) { article ->
            NewsCard(
                article = article,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (filtered.isEmpty()) {
            item {
                val message = if (searchQuery.isNotEmpty()) "${NewsStrings.NO_NEWS_FOUND}$searchQuery'"
                else NewsStrings.NO_NEWS_AVAILABLE
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
