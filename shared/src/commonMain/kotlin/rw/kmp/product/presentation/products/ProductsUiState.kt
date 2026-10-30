package rw.kmp.product.presentation.products

import org.jetbrains.compose.resources.StringResource
import productapp.shared.generated.resources.Res
import productapp.shared.generated.resources.products_title
import rw.kmp.product.domain.model.Product

sealed interface ProductsUiState {
    val title: StringResource
        get() = Res.string.products_title

    data object Loading : ProductsUiState

    data class Success(
        val products: List<Product>,
        val isRefreshing: Boolean = false,
        val refreshError: StringResource? = null,
    ) : ProductsUiState

    data class Empty(
        val isRefreshing: Boolean = false,
        val refreshError: StringResource? = null,
    ) : ProductsUiState

    data class Error(
        val message: StringResource,
    ) : ProductsUiState
}
