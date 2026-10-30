package rw.kmp.product.domain.usecase

import rw.kmp.product.domain.model.Product
import rw.kmp.product.domain.repository.ProductRepository

class GetProductsUseCase(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(): Result<List<Product>> = repository.getProducts()
}
