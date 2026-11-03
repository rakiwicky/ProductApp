# Tasks: Product Detail Screen

## Status
- Implementing: product tap navigation and detail screen flow

## Phase 1: Navigation contract
- [X] T001 Define the shared `ProductDestination` flow and `ProductNavigator` interface so the app can move between products list and detail screen.
- [X] T002 Ensure the navigator is registered in Koin with a real `DefaultProductNavigator` implementation instead of the no-op default.

## Phase 2: App-level routing
- [X] T003 Update the shared `App()` root to watch `navigator.currentDestination` and render either `ProductsScreen` or `ProductDetailScreen`.
- [X] T004 Wire product taps in `ProductsViewModel.onProductClick()` to call `navigator.openProductDetail(product)`.

## Phase 3: Detail screen UI
- [X] T005 Create a product detail screen that renders the selected product image prominently, its title/category/price, description, and a back action.
- [X] T006 Keep the detail view resilient with image fallback behavior and a back-to-products action.

## Phase 4: Verification
- [ ] T007 Run the shared tests and Android/iOS build checks to confirm navigation and UI compile cleanly.

