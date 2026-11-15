package rw.kmp.product.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import rw.kmp.product.data.network.HttpClientFactory
import rw.kmp.product.data.network.ProductApi
import rw.kmp.product.domain.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ProductRepositoryImplTest {
    @Test
    fun mapsAllDtoFieldsToDomainProduct() = runTest {
        val body = """
            [{
              "id": 7,
              "title": "Canvas bag",
              "price": 24.5,
              "description": "A sturdy bag",
              "category": "accessories",
              "image": "https://example.com/bag.png",
              "rating": {"rate": 4.5, "count": 10}
            }]
        """.trimIndent()

        val result = repositoryFor(body).getProducts()

        assertEquals(
            listOf(
                Product(
                    id = 7,
                    title = "Canvas bag",
                    price = 24.5,
                    description = "A sturdy bag",
                    category = "accessories",
                    imageUrl = "https://example.com/bag.png",
                ),
            ),
            result.getOrThrow(),
        )
    }

    @Test
    fun returnsFailureForHttpError() = runTest {
        val result = repositoryFor(
            body = """{"error":"unavailable"}""",
            status = HttpStatusCode.ServiceUnavailable,
        ).getProducts()

        assertTrue(result.isFailure)
    }

    @Test
    fun returnsFailureForInvalidResponseBody() = runTest {
        val result = repositoryFor(body = "not-json").getProducts()

        assertTrue(result.isFailure)
    }

    @Test
    fun rethrowsCancellation() = runTest {
        val cancellation = CancellationException("Test cancellation")
        val repository = ProductRepositoryImpl(
            ProductApi(
                HttpClientFactory.create(
                    MockEngine { throw cancellation },
                ),
            ),
        )

        val thrown = assertFailsWith<CancellationException> {
            repository.getProducts()
        }
        assertEquals(cancellation.message, thrown.message)
    }

    private fun repositoryFor(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): ProductRepositoryImpl {
        val client = HttpClientFactory.create(
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        )
        return ProductRepositoryImpl(ProductApi(client))
    }
}
