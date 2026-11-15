package rw.kmp.product

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import rw.kmp.product.di.appModules
import rw.kmp.product.presentation.products.ProductsScreen

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration {
            modules(appModules)
        },
    ) {
        MaterialTheme {
            ProductsScreen()
        }
    }
}