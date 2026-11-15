# Data Model: Product Search

## Core entities

### Product

Represents one catalog item already returned by the list domain model.

| Field | Type | Notes |
|---|---|---|
| id | Long | Stable identifier used as list key and navigation target |
| title | String | Matched against the query for title search |
| category | String | Matched against the query for category search |
| price | Double | Matched against the query for price search |
| description | String | Not used in search; retained for product details |
| imageUrl | String | Rendered in the product card |

### SearchQuery

Represents the current text entered by the user, normalized before filtering.

| Field | Type | Notes |
|---|---|---|
| rawText | String | Current query from the text field |
| normalizedText | String | Trimmed, lower-case form used for matching |

### ProductsUiState

The state contract used by the shared screen and ViewModel.

- `Loading`
- `Success(products, isRefreshing, refreshError, searchQuery)`
- `Empty(isRefreshing, refreshError, searchQuery)`
- `Error(message)`

Rules:
- Query state is preserved even when the result list is empty.
- A blank or whitespace-only query restores the full catalog.
- Clear action resets `searchQuery` to an empty string.

## Relationships

- One `ProductsUiState.Success` or `ProductsUiState.Empty` contains a filtered subset of the full catalog.
- The full catalog remains available to the ViewModel, even when the visible list is filtered.
- The composable renders the current `searchQuery` and passes input updates into `onSearchChanged`.

## Validation rules

- Empty query: show all products.
- Whitespace-only query: treat as empty.
- Search is case-insensitive.
- Result list emptiness triggers the empty-state UI while preserving the current query.
- Clear-button action sets query to empty and restores the unfiltered list.

