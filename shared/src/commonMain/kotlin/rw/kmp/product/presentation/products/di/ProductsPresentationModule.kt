package rw.kmp.product.presentation.products.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rw.kmp.product.presentation.products.ProductsViewModel

val productsPresentationModule = module {
    viewModelOf(::ProductsViewModel)
}
