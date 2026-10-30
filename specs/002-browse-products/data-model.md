# Data Model: Browse Products

## Domain Entity: Product

Represents one item returned by the store catalog and displayed in the Products screen.

| Field | Type | Required | Notes |
|---|---|---:|---|
| `id` | `Int` | Yes | Stable catalog identifier and lazy-list key. |
| `title` | `String` | Yes | Product title; UI truncates visual display to two lines. |
| `price` | `Double` | Yes | Numeric amount supplied by FakeStoreAPI; displayed as USD with two decimal places. |
| `description` | `String` | Yes | API/domain detail data; not rendered in this feature. |
| `category` | `String` | Yes | Category label shown in each product row. |
| `imageUrl` | `String` | Yes | Remote image address; image-load failure does not invalidate the remaining product fields. |

### Validation and Mapping

- Map DTO field `image` to domain `imageUrl`; other named fields map directly.
- The API response does not contain a currency field; product prices are interpreted as USD for display.
- A successful empty response is valid and produces an empty product collection.
- An image request failure is a presentation fallback, not a catalog request failure.
- HTTP errors, invalid response bodies, and serialization errors produce a failed repository result. Cancellation is rethrown, not converted to a normal failure.

## Data Transfer Object: ProductDto

The serializable transport representation mirrors `id`, `title`, `price`, `description`, `category`, and `image`. Unknown response fields (for example `rating`) are ignored by the shared JSON configuration. DTOs and Ktor types remain inside the data layer.

## Repository and Use Case

- **ProductRepository**: Domain contract to retrieve the current full product catalog as `Result<List<Product>>`.
- **ProductRepositoryImpl**: Fetches and decodes the API response, maps each DTO to `Product`, and translates transport/decoding failures into failed results.
- **GetProductsUseCase**: Delegates catalog retrieval to `ProductRepository`; presentation depends on this use case and not on the repository implementation.

## Presentation State

`ProductsViewModel` exposes exactly one `StateFlow<ProductsUiState>`:

| State | Meaning |
|---|---|
| `Loading` | Initial request is in progress. |
| `Success(products, isRefreshing, refreshError)` | Non-empty catalog content; refresh may be active or have failed without discarding products. |
| `Empty(isRefreshing, refreshError)` | Successful empty catalog; refresh feedback can be represented without replacing the empty result. |
| `Error(message)` | Initial request failed; UI offers retry through `load()`. |

`load()` transitions from initial loading to `Success`, `Empty`, or `Error`. `refresh()` keeps current content visible while requesting; success transitions to the latest `Success`/`Empty`, and failure preserves current content with a refresh-specific message.
