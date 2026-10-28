# Research: Browse Products

**Date**: 2026-10-28

## Decisions

### Cross-platform HTTP client

- **Decision**: Use Ktor client core and content negotiation in `commonMain`; provide OkHttp on Android and Darwin on iOS. Create/configure the shared `HttpClient` from the data Koin module using a platform-selected engine.
- **Rationale**: Ktor documents client core plus a platform engine per source set and provides the requested engine options for Android and Kotlin/Native.
- **Alternatives considered**: Ktor's multi-platform CIO engine and platform-default engines. Use the explicitly constitution-mandated OkHttp/Darwin engines to match project policy and keep platform-specific engine dependencies out of `commonMain`.
- **References**: [Ktor multiplatform client setup](https://ktor.io/docs/client-create-multiplatform-application.html), [Ktor client engines](https://ktor.io/docs/client-engines.html).

### JSON decoding and response compatibility

- **Decision**: Use `ContentNegotiation` with kotlinx.serialization and configure shared `Json` with `ignoreUnknownKeys = true`. Keep the DTO fields limited to the requested API fields.
- **Rationale**: Ktor's content-negotiation integration supports kotlinx.serialization. The observed product response includes `rating` beyond the requested data; ignoring unknown keys allows the six-field DTO to decode it without coupling the domain model to unrelated response data.
- **Alternatives considered**: Add all API extras to the DTO/domain model or hand-parse response strings. Extra fields are not required by the product screen; manual parsing would duplicate standard serialization behavior.
- **References**: [Ktor client serialization](https://ktor.io/docs/client-serialization.html), [FakeStoreAPI product example](https://fakestoreapi.com/products/1).

### Dependency injection and platform boundaries

- **Decision**: Define one Koin module for each of domain, data, and presentation in `commonMain`. Compose them at the shared `App()` root using Koin's Compose lifecycle integration. Supply a platform engine factory from Android/iOS source sets.
- **Rationale**: Koin supports common KMP modules, shared startup, and platform-specific definitions. A platform engine factory keeps engine dependencies and types outside domain/presentation and avoids introducing another Gradle module.
- **Alternatives considered**: Start a second global Koin instance from each platform entry point or add Android-specific Koin setup. Both conflict with the requested `App()` composition root and add platform-specific wiring to a feature shared by both targets.
- **References**: [Koin KMP setup](https://insert-koin.io/docs/reference/koin-core/kmp-setup/), [Koin KMP integration](https://insert-koin.io/docs/reference/koin-mp/kmp/).

### Product image loading and refresh interaction

- **Decision**: Render remote images with Coil 3 `AsyncImage`, with a neutral error/fallback painter and product-aware accessibility text. Wrap the stable-key `LazyColumn` in Material 3 `PullToRefreshBox`.
- **Rationale**: Coil recommends `AsyncImage` for constrained Compose content and supports placeholder/error painters. Material 3's pull-to-refresh box takes `isRefreshing` and an `onRefresh` callback around scrollable content.
- **Alternatives considered**: Load images manually or use a custom drag detector. These duplicate image/network or gesture handling and do not improve the stated requirements.
- **References**: [Coil Compose](https://coil-kt.github.io/coil/compose/), [Material 3 pull to refresh](https://developer.android.com/develop/ui/compose/components/pull-to-refresh).

### Refresh failure state representation

- **Decision**: Keep the requested sealed state categories. Represent progress or a refresh error alongside existing `Success`/`Empty` content; use `Error` for initial load failures.
- **Rationale**: A single exclusive `Error` state after a failed refresh would discard the visible list and contradict the specification. Metadata on content states keeps the single sealed `StateFlow` while allowing the refresh indicator and retry feedback.
- **Alternatives considered**: Replace content with `Error` for all failures or add a separate independent error flow. The first loses products; the second violates the constitution's single-UiState requirement.

### Product currency

- **Decision**: Treat FakeStoreAPI numeric prices as USD and display them consistently with `$` and two decimal places.
- **Rationale**: The selected endpoint returns a numeric `price` without a currency code; the catalog must assign one consistent display currency. The sample product response likewise contains no currency property.
- **Alternatives considered**: Guess a currency at each render or add a currency field not supplied by the endpoint. Both make display inconsistent or fabricate response data. If a different store currency is required later, it must be supplied as product/catalog configuration.
- **Reference**: [FakeStoreAPI product example](https://fakestoreapi.com/products/1).

### Tests

- **Decision**: Use `FakeProductRepository` for use-case and ViewModel tests; use Turbine for `StateFlow` transitions and Ktor MockEngine to validate repository requests, response mapping, and failure handling.
- **Rationale**: These are multiplatform-compatible tests aligned with the constitution's fakes-first rule. Turbine provides deterministic collection assertions for flows.
- **Alternatives considered**: JVM-only mocking or tests against the live API. Mocks are prohibited in shared tests and live endpoint tests would be network-dependent and nondeterministic.
- **References**: [Turbine](https://github.com/cashapp/turbine), [kotlinx-coroutines-test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/).

## Resolved Unknowns

- Target engine choice is fixed by the constitution: OkHttp on Android and Darwin on iOS.
- The endpoint supplies product prices but no currency code; this plan uses USD as its display currency.
- The project currently has no Ktor, serialization, Koin, Coil, or coroutine test dependencies; add compatible versions through the version catalog before use.
