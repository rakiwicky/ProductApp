package rw.kmp.product.domain.usecase

import kotlinx.coroutines.test.runTest
import rw.kmp.product.domain.model.Product
import rw.kmp.product.domain.repository.ProductRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeProductRepository(
    var result: Result<List<Product>>,
) : ProductRepository {
    var calls: Int = 0
        private set

    override suspend fun getProducts(): Result<List<Product>> {
        calls += 1
        return result
    }
}

class GetProductsUseCaseTest {
    private val product = Product(
        id = 1,
        title = "Bag",
        price = 12.5,
        description = "A bag",
        category = "accessories",
        imageUrl = "https://example.com/bag.png",
    )

    @Test
    fun returnsRepositorySuccess() = runTest {
        val repository = FakeProductRepository(Result.success(listOf(product)))

        val result = GetProductsUseCase(repository)()

        assertTrue(result.isSuccess)
        assertEquals(listOf(product), result.getOrNull())
        assertEquals(1, repository.calls)
    }

    @Test
    fun returnsRepositoryFailure() = runTest {
        val failure = IllegalStateException("Catalog unavailable")
        val repository = FakeProductRepository(Result.failure(failure))

        val result = GetProductsUseCase(repository)()

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
        assertEquals(1, repository.calls)
        assertFalse(result.isSuccess)
    }
}
