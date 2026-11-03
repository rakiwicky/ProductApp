package rw.kmp.product.navigation

import rw.kmp.product.domain.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductNavigatorTest {
    @Test
    fun inMemoryNavigatorTracksRouteChanges() {
        val product = Product(
            id = 42,
            title = "Test product",
            price = 12.5,
            description = "A product",
            category = "gear",
            imageUrl = "https://example.com/test.png",
        )
        val navigator = InMemoryProductNavigator()

        navigator.openProductDetail(product)
        navigator.back()

        assertEquals(ProductDestination.Products, navigator.currentDestination.value)
        assertEquals(listOf("openProductDetail:42", "back"), navigator.events)
    }
}
