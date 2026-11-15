# Research: Product Search

## Decision: local, case-insensitive filtering in the shared ViewModel

The search feature should not add a new network layer or backend query. The product catalog already exists in memory in the shared `ProductsViewModel`, so the simplest valid implementation is to keep the full catalog and filter it locally as the user types.

### Rationale
- The app already has a loaded catalog in the shared presentation layer.
- Search by title, category, and price is a pure client-side filter and matches the current architecture.
- Filtering in `commonMain` preserves platform parity and keeps behavior consistent on Android and iOS.
- It avoids introducing new data-fetching semantics or network contracts for a screen-local feature.

### Alternatives considered
- Server-side filtering: rejected because the product list is already loaded locally and no backend search API exists.
- Filtering in the composable layer: rejected because state and business logic should remain in the `ViewModel` to satisfy MVVM and testability.
- Separate search repository/state: rejected as over-engineered for a single screen with a local list.

## Decision: search state lives in `ProductsUiState`

The UI state needs to preserve both the filtered results and the current search query so the screen can render the list, empty state, and clear button consistently.

### Rationale
- The UI must know whether the query is empty or non-empty.
- Empty state logic depends on whether the filtered list is empty, not just on the raw catalog state.
- The clear button can be shown based on non-empty query state without duplicating logic in the composable.

## Decision: price matching uses the displayed numeric product value

Price matching should compare against the existing price value already shown in the list, using the numeric value as a string search target.

### Rationale
- Product price is already a domain field on `Product`.
- The feature requirement explicitly includes price search without introducing a new search schema.
- Matching by the rendered `price` value keeps the path simple and consistent with the rest of the screen.

## Decision: clear button resets the query and restores the full catalog immediately

The clear action should be a first-class UI affordance whenever the query is non-empty, and it should issue a single, explicit reset to the empty query state.

### Rationale
- This matches the clarified spec requirement and user expectation.
- It is easy to validate and test in the ViewModel and UI.
- It keeps the feature behavior consistent with the empty-state flow and avoids stale filtered results after clearing.

## Open technical decisions resolved
- Search is local and shared: resolved.
- Matching is case-insensitive: resolved.
- Search target fields are title, category, and price: resolved.
- Empty state and clear behavior are part of the screen contract: resolved.

