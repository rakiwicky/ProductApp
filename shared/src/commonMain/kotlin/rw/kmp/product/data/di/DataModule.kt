package rw.kmp.product.data.di

import org.koin.dsl.module
import rw.kmp.product.data.network.HttpClientFactory
import rw.kmp.product.data.network.ProductApi
import rw.kmp.product.data.repository.ProductRepositoryImpl
import rw.kmp.product.domain.repository.ProductRepository

val dataModule = module {
    single { HttpClientFactory.create() }
    single { ProductApi(get()) }
    single<ProductRepository> { ProductRepositoryImpl(get()) }
}
