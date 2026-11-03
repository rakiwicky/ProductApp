package rw.kmp.product.navigation

interface AppNavigator {
    val productNavigator: ProductNavigator
}

class DefaultAppNavigator(
    override val productNavigator: ProductNavigator = DefaultProductNavigator(),
) : AppNavigator
