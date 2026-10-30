package rw.kmp.product.data.repository

import kotlinx.coroutines.CancellationException
import rw.kmp.product.data.mapper.toDomain
import rw.kmp.product.data.network.ProductApi
import rw.kmp.product.domain.model.Product
import rw.kmp.product.domain.repository.ProductRepository

internal class ProductRepositoryImpl(
    private val productApi: ProductApi,
) : ProductRepository {
    override suspend fun getProducts(): Result<List<Product>> =
        try {
            Result.success(productApi.getProducts().map { it.toDomain() })
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            Result.failure(exception)
        }
}
