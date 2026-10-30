package rw.kmp.product.domain.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import rw.kmp.product.domain.usecase.GetProductsUseCase

val domainModule = module {
    factoryOf(::GetProductsUseCase)
}
