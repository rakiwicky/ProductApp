package rw.kmp.product.domain.repository

import rw.kmp.product.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>
}
