package rw.kmp.product.di

import org.koin.dsl.module
import rw.kmp.product.data.di.dataModule
import rw.kmp.product.domain.di.domainModule
import rw.kmp.product.navigation.DefaultProductNavigator
import rw.kmp.product.navigation.ProductNavigator
import rw.kmp.product.presentation.products.di.productsPresentationModule

val navigationModule = module {
    single<ProductNavigator> { DefaultProductNavigator() }
}

val appModules = listOf(
    domainModule,
    dataModule,
    navigationModule,
    productsPresentationModule,
)
