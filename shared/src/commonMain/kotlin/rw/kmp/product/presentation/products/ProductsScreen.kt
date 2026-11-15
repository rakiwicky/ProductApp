package rw.kmp.product.presentation.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import productapp.shared.generated.resources.Res
import productapp.shared.generated.resources.products_empty
import productapp.shared.generated.resources.products_retry
import rw.kmp.product.domain.model.Product
import rw.kmp.product.navigation.NoOpProductNavigator
import rw.kmp.product.navigation.ProductNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel = koinViewModel(),
    navigator: ProductNavigator = NoOpProductNavigator(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val refreshError = when (state) {
        is ProductsUiState.Success -> state.refreshError
        is ProductsUiState.Empty -> state.refreshError
        else -> null
    }
    val refreshErrorText = refreshError?.let { stringResource(it) }
    val retryLabel = stringResource(Res.string.products_retry)
    LaunchedEffect(refreshError) {
        if (refreshErrorText != null) {
            val result = snackbarHostState.showSnackbar(
                message = refreshErrorText,
                actionLabel = retryLabel,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(state.title)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        when (val uiState = state) {
            ProductsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProductsUiState.Success -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    verticalArrangement = Arrangement.Top,
                ) {
                    SearchField(
                        query = uiState.searchQuery,
                        onValueChange = viewModel::onSearchChanged,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        ProductList(
                            products = uiState.products,
                            onProductClick = viewModel::onProductClick,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            is ProductsUiState.Empty -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    verticalArrangement = Arrangement.Top,
                ) {
                    SearchField(
                        query = uiState.searchQuery,
                        onValueChange = viewModel::onSearchChanged,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                CenteredMessage(
                                    message = stringResource(Res.string.products_empty),
                                    modifier = Modifier.fillParentMaxSize(),
                                )
                            }
                        }
                    }
                }
            }

            is ProductsUiState.Error -> CenteredMessage(
                message = stringResource(uiState.message),
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                actionLabel = stringResource(Res.string.products_retry),
                onAction = viewModel::load,
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onValueChange,
        modifier = modifier,
        trailingIcon = {
            if (query.isNotEmpty()) {
                TextButton(onClick = { onValueChange("") }) {
                    Text("Clear")
                }
            }
        },
        singleLine = true,
        placeholder = { Text("Search products") },
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun ProductList(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = products,
            key = { product -> product.id },
        ) { product ->
            ProductRow(
                product = product,
                onClick = { onProductClick(product) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fallbackColor = MaterialTheme.colorScheme.surfaceVariant
    val fallbackPainter = remember(fallbackColor) { ColorPainter(fallbackColor) }

    Row(
        modifier = modifier.clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.title,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(fallbackColor),
            contentScale = ContentScale.Crop,
            error = fallbackPainter,
            placeholder = fallbackPainter,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = product.category,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = formatUsdPrice(product.price),
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
private fun CenteredMessage(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
