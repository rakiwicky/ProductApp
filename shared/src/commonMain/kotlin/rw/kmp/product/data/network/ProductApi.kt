package rw.kmp.product.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import rw.kmp.product.data.dto.ProductDto

internal class ProductApi(
    private val httpClient: HttpClient,
) {
    suspend fun getProducts(): List<ProductDto> {
        val response = httpClient.get(PRODUCTS_URL)
        check(response.status.value in 200..299) {
            "Product request failed with HTTP ${response.status.value}."
        }
        return response.body()
    }

    private companion object {
        const val PRODUCTS_URL = "https://fakestoreapi.com/products"
    }
}
