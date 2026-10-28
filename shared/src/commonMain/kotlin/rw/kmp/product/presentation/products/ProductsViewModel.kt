package rw.kmp.product.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import productapp.shared.generated.resources.Res
import productapp.shared.generated.resources.products_load_error
import rw.kmp.product.domain.usecase.GetProductsUseCase

class ProductsViewModel(
    private val getProducts: GetProductsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductsUiState>(ProductsUiState.Loading)
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProductsUiState.Loading
            val result = getProducts()
            _uiState.value = result.fold(
                onSuccess = { ProductsUiState.Success(it) },
                onFailure = { ProductsUiState.Error(Res.string.products_load_error) },
            )
        }
    }
}
