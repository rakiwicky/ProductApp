package rw.kmp.product.presentation.products

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import productapp.shared.generated.resources.Res
import productapp.shared.generated.resources.products_load_error
import productapp.shared.generated.resources.products_refresh_error
import productapp.shared.generated.resources.products_title
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

    private val updatedProduct = Product(
        id = 2,
        title = "Shoes",
        price = 40.0,
        description = "Running shoes",
        category = "footwear",
        imageUrl = "https://example.com/shoes.png",
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadingStateExposesProductsTitleResource() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = ProductsViewModel(
                GetProductsUseCase(
                    FakeProductRepository(Result.success(listOf(product))),
                ),
            )

            assertEquals(Res.string.products_title, viewModel.uiState.value.title)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorStateExposesLoadErrorResource() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = ProductsViewModel(
                GetProductsUseCase(
                    FakeProductRepository(Result.failure(IllegalStateException("offline"))),
                ),
            )

            viewModel.load()
            runCurrent()

            val errorState = viewModel.uiState.value as ProductsUiState.Error
            assertEquals(Res.string.products_load_error, errorState.message)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadEmitsEmptyForSuccessfulEmptyResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.success(emptyList()))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))

            viewModel.uiState.test {
                assertEquals(ProductsUiState.Loading, awaitItem())

                viewModel.load()
                runCurrent()

                assertEquals(ProductsUiState.Empty(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadEmitsErrorAfterInitialLoadingFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.failure(IllegalStateException("offline")))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))

            viewModel.uiState.test {
                assertEquals(ProductsUiState.Loading, awaitItem())

                viewModel.load()
                runCurrent()

                assertEquals(ProductsUiState.Error(Res.string.products_load_error), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun retryAfterFailureShowsLoadingThenProducts() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.failure(IllegalStateException("offline")))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))

            viewModel.uiState.test {
                assertEquals(ProductsUiState.Loading, awaitItem())
                viewModel.load()
                runCurrent()
                assertEquals(ProductsUiState.Error(Res.string.products_load_error), awaitItem())

                repository.result = Result.success(listOf(product))
                viewModel.load()
                runCurrent()

                assertEquals(ProductsUiState.Loading, awaitItem())
                assertEquals(ProductsUiState.Success(listOf(product)), awaitItem())
                assertEquals(2, repository.calls)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun retryAfterFailureShowsLoadingThenEmpty() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.failure(IllegalStateException("offline")))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))

            viewModel.uiState.test {
                assertEquals(ProductsUiState.Loading, awaitItem())
                viewModel.load()
                runCurrent()
                assertEquals(ProductsUiState.Error(Res.string.products_load_error), awaitItem())

                repository.result = Result.success(emptyList())
                viewModel.load()
                runCurrent()

                assertEquals(ProductsUiState.Loading, awaitItem())
                assertEquals(ProductsUiState.Empty(), awaitItem())
                assertEquals(2, repository.calls)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun refreshShowsProgressThenReplacesProducts() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.success(listOf(product)))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))

            viewModel.uiState.test {
                assertEquals(ProductsUiState.Loading, awaitItem())
                viewModel.load()
                runCurrent()
                assertEquals(ProductsUiState.Success(listOf(product)), awaitItem())

                repository.result = Result.success(listOf(updatedProduct))
                viewModel.refresh()
                runCurrent()

                assertEquals(ProductsUiState.Success(listOf(product), isRefreshing = true), awaitItem())
                assertEquals(ProductsUiState.Success(listOf(updatedProduct)), awaitItem())
                assertEquals(2, repository.calls)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun refreshSuccessWithNoProductsShowsEmpty() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.success(listOf(product)))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))

            viewModel.uiState.test {
                assertEquals(ProductsUiState.Loading, awaitItem())
                viewModel.load()
                runCurrent()
                assertEquals(ProductsUiState.Success(listOf(product)), awaitItem())

                repository.result = Result.success(emptyList())
                viewModel.refresh()
                runCurrent()

                assertEquals(ProductsUiState.Success(listOf(product), isRefreshing = true), awaitItem())
                assertEquals(ProductsUiState.Empty(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun refreshFromEmptyShowsProgressThenProducts() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.success(emptyList()))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))

            viewModel.uiState.test {
                assertEquals(ProductsUiState.Loading, awaitItem())
                viewModel.load()
                runCurrent()
                assertEquals(ProductsUiState.Empty(), awaitItem())

                repository.result = Result.success(listOf(product))
                viewModel.refresh()
                runCurrent()

                assertEquals(ProductsUiState.Empty(isRefreshing = true), awaitItem())
                assertEquals(ProductsUiState.Success(listOf(product)), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun refreshFailurePreservesProductsWithRetryableError() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.success(listOf(product)))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))

            viewModel.uiState.test {
                assertEquals(ProductsUiState.Loading, awaitItem())
                viewModel.load()
                runCurrent()
                assertEquals(ProductsUiState.Success(listOf(product)), awaitItem())

                repository.result = Result.failure(IllegalStateException("offline"))
                viewModel.refresh()
                runCurrent()

                assertEquals(ProductsUiState.Success(listOf(product), isRefreshing = true), awaitItem())
                assertEquals(
                    ProductsUiState.Success(
                        products = listOf(product),
                        refreshError = Res.string.products_refresh_error,
                    ),
                    awaitItem(),
                )

                // Retrying the refresh clears the error and can succeed.
                repository.result = Result.success(listOf(updatedProduct))
                viewModel.refresh()
                runCurrent()

                assertEquals(ProductsUiState.Success(listOf(product), isRefreshing = true), awaitItem())
                assertEquals(ProductsUiState.Success(listOf(updatedProduct)), awaitItem())
                assertEquals(3, repository.calls)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun refreshIsIgnoredWhileAlreadyRefreshing() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeProductRepository(Result.success(listOf(product)))
            val viewModel = ProductsViewModel(GetProductsUseCase(repository))
            viewModel.load()
            runCurrent()

            viewModel.refresh()
            viewModel.refresh()
            runCurrent()

            assertEquals(2, repository.calls)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
