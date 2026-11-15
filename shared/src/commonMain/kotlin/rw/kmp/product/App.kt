package rw.kmp.product

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration
import rw.kmp.product.di.appModules
import rw.kmp.product.navigation.ProductDestination
import rw.kmp.product.navigation.ProductNavigator
import rw.kmp.product.presentation.products.ProductsScreen
import rw.kmp.product.presentation.products.ProductDetailScreen

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration {
            modules(appModules)
        },
    ) {
        val navigator = koinInject<ProductNavigator>()
        val destination by navigator.currentDestination.collectAsState()

        MaterialTheme {
            when (val screen = destination) {
                ProductDestination.Products -> ProductsScreen()
                is ProductDestination.ProductDetail -> ProductDetailScreen(screen.product, onBack = navigator::back)
            }
        }
    }
}