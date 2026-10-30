# Products API Contract

## Request

- **Method**: `GET`
- **URL**: `https://fakestoreapi.com/products`
- **Request body**: None
- **Authentication**: None required for this feature.
- **Platform access**: Android requires the `INTERNET` permission. iOS requires no additional networking permission.

## Successful Response

- **Status**: HTTP success
- **Body**: JSON array of products.
- **Fields used**:

| Field | JSON type | Domain mapping |
|---|---|---|
| `id` | integer | `Product.id` |
| `title` | string | `Product.title` |
| `price` | number | `Product.price` |
| `description` | string | `Product.description` |
| `category` | string | `Product.category` |
| `image` | string (URL) | `Product.imageUrl` |

Unknown response keys are ignored. The API response contains no currency code; this feature interprets prices as USD. A successful empty array is the empty catalog result.

## Failure Behavior

- Non-success HTTP responses, transport failures, and invalid JSON become repository failures and are exposed to the UI as an initial error with retry or a refresh error while retaining currently displayed content.
- Coroutine cancellation is propagated and is not surfaced as a user-facing network error.
- Failure to load an individual image does not fail the catalog request; the UI displays a neutral image placeholder.

## App-Facing Boundary

The app's internal domain boundary is `ProductRepository.getProducts(): Result<List<Product>>`, called through `GetProductsUseCase`. No public network endpoint is exposed by this app.
