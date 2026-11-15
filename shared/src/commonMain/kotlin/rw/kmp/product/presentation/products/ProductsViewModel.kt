package rw.kmp.product.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import productapp.shared.generated.resources.Res
import productapp.shared.generated.resources.products_load_error
import productapp.shared.generated.resources.products_refresh_error
import rw.kmp.product.domain.model.Product
import rw.kmp.product.domain.usecase.GetProductsUseCase
import rw.kmp.product.navigation.NoOpProductNavigator
import rw.kmp.product.navigation.ProductNavigator

class ProductsViewModel(
    private val getProducts: GetProductsUseCase,
    private val navigator: ProductNavigator = NoOpProductNavigator(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductsUiState>(ProductsUiState.Loading)
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    fun onProductClick(product: Product) {
        navigator.openProductDetail(product)
    }

    /** Initial load and retry after an initial failure. */
    fun load() {
        viewModelScope.launch {
            _uiState.value = ProductsUiState.Loading
            _uiState.value = getProducts().fold(
                onSuccess = ::contentState,
                onFailure = { ProductsUiState.Error(Res.string.products_load_error) },
            )
        }
    }

    /** Refreshes loaded content; keeps it visible if the refresh fails. */
    fun refresh() {
        val current = _uiState.value
        _uiState.value = when (current) {
            is ProductsUiState.Success ->
                if (current.isRefreshing) return
                else current.copy(isRefreshing = true, refreshError = null)
            is ProductsUiState.Empty ->
                if (current.isRefreshing) return
                else current.copy(isRefreshing = true, refreshError = null)
            ProductsUiState.Loading, is ProductsUiState.Error -> return
        }

        viewModelScope.launch {
            _uiState.value = getProducts().fold(
                onSuccess = ::contentState,
                onFailure = {
                    when (val previous = _uiState.value) {
                        is ProductsUiState.Success -> previous.copy(
                            isRefreshing = false,
                            refreshError = Res.string.products_refresh_error,
                        )
                        is ProductsUiState.Empty -> previous.copy(
                            isRefreshing = false,
                            refreshError = Res.string.products_refresh_error,
                        )
                        else -> previous
                    }
                },
            )
        }
    }

    private fun contentState(products: List<Product>): ProductsUiState =
        if (products.isEmpty()) ProductsUiState.Empty() else ProductsUiState.Success(products)
}
