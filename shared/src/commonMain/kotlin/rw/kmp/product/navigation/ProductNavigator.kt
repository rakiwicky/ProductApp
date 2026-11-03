package rw.kmp.product.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rw.kmp.product.domain.model.Product

sealed interface ProductDestination {
    data object Products : ProductDestination
    data class ProductDetail(val product: Product) : ProductDestination
}

interface ProductNavigator {
    val currentDestination: StateFlow<ProductDestination>
    fun openProductDetail(product: Product)
    fun back()
}

class NoOpProductNavigator : ProductNavigator {
    override val currentDestination: StateFlow<ProductDestination> =
        MutableStateFlow(ProductDestination.Products).asStateFlow()

    override fun openProductDetail(product: Product) = Unit
    override fun back() = Unit
}

class DefaultProductNavigator : ProductNavigator {
    private val _currentDestination = MutableStateFlow<ProductDestination>(ProductDestination.Products)
    override val currentDestination: StateFlow<ProductDestination> = _currentDestination.asStateFlow()

    override fun openProductDetail(product: Product) {
        _currentDestination.value = ProductDestination.ProductDetail(product)
    }

    override fun back() {
        _currentDestination.value = ProductDestination.Products
    }
}

class InMemoryProductNavigator : ProductNavigator {
    private val _currentDestination = MutableStateFlow<ProductDestination>(ProductDestination.Products)
    override val currentDestination: StateFlow<ProductDestination> = _currentDestination.asStateFlow()

    private val _events = mutableListOf<String>()
    val events: List<String>
        get() = _events.toList()

    override fun openProductDetail(product: Product) {
        _events += "openProductDetail:${product.id}"
        _currentDestination.value = ProductDestination.ProductDetail(product)
    }

    override fun back() {
        _events += "back"
        _currentDestination.value = ProductDestination.Products
    }
}
