package rw.kmp.product.presentation.products

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import rw.kmp.product.domain.model.Product
import rw.kmp.product.domain.usecase.FakeProductRepository
import rw.kmp.product.domain.usecase.GetProductsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductsViewModelTest {
    private val product = Product(
        id = 1,
        title = "Bag",
        price = 12.5,
        description = "A bag",
        category = "accessories",
        imageUrl = "https://example.com/bag.png",
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadEmitsSuccessAfterInitialLoading() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.success(listOf(product)))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))

            viewModel.uiState.test {
                assertEquals(ProductsUiState.Loading, awaitItem())

                viewModel.load()
                runCurrent()

                assertEquals(ProductsUiState.Success(listOf(product)), awaitItem())
                assertEquals(1, repository.calls)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
}
