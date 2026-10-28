package rw.kmp.product.di

import rw.kmp.product.data.di.dataModule
import rw.kmp.product.domain.di.domainModule
import rw.kmp.product.presentation.products.di.productsPresentationModule

val appModules = listOf(
    domainModule,
    dataModule,
    productsPresentationModule,
)
