# Product Search UI Contract

## Screen-level contract

The product list screen exposes a search field and receives text updates through the shared ViewModel.

### Inputs

- `onSearchChanged(query: String)`
- `onProductClick(product: Product)`
- `refresh()`
- `load()`

### State

```kotlin
sealed interface ProductsUiState {
    data object Loading : ProductsUiState
    data class Success(
        val products: List<Product>,
        val isRefreshing: Boolean = false,
        val refreshError: StringResource? = null,
        val searchQuery: String = "",
    ) : ProductsUiState
    data class Empty(
        val isRefreshing: Boolean = false,
        val refreshError: StringResource? = null,
        val searchQuery: String = "",
    ) : ProductsUiState
    data class Error(
        val message: StringResource,
    ) : ProductsUiState
}
```

### Behavior

- Non-empty query shows a clear action.
- Clear action resets `searchQuery` to empty and restores the full list.
- Empty query renders all products.
- Empty results render the no-results message and preserve the current query until cleared.
- Refresh and load flows remain available without interfering with search behavior.

