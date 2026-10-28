# Implementation Plan: Browse Products

**Branch**: `002-browse-products` | **Date**: 2026-10-28 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-browse-products/spec.md`

## Summary

Build a shared Android/iOS Products screen that loads the catalog from `GET https://fakestoreapi.com/products`, maps the response to a domain `Product`, and renders product imagery, title, category, and USD price. Include a "Products" top app bar and respect Android edge-to-edge system bars and iOS safe areas. Keep the requested domain/data/presentation boundaries within the existing `shared` Gradle module, expose one Koin module per layer, and initialize the graph at the shared `App()` root. Provide initial loading/error/empty states, retry, and pull-to-refresh while retaining already displayed products after a refresh failure.

## Technical Context

**Language/Version**: Kotlin Multiplatform; Kotlin 2.4.21 from the existing version catalog.

**Primary Dependencies**: Compose Multiplatform 1.12.2 and Material 3 1.9.0; add version-catalog-managed Ktor client core, content negotiation, kotlinx.serialization JSON, OkHttp and Darwin engines, Koin core/Compose/ViewModel integration, Coil 3 Compose and Ktor 3 network fetcher, kotlinx-coroutines-test, Turbine, and Ktor MockEngine. Add the Kotlin serialization plugin using a version aligned with Kotlin.

**Storage**: None. Product data is loaded from the remote endpoint and is not persisted.

**Testing**: `kotlin.test`, `kotlinx-coroutines-test`, and Turbine in `commonTest`; hand-written `FakeProductRepository` for use-case/ViewModel tests; Ktor MockEngine for repository request and response behavior; mapper tests for DTO-to-domain conversion.

**Target Platform**: Android and iOS (`iosArm64`, `iosSimulatorArm64`); all feature behavior and UI in `shared/src/commonMain`.

**Project Type**: Kotlin Multiplatform mobile app with a shared Compose UI. The repository currently has `:shared` and `:androidApp`; data/domain/presentation are package boundaries, not new Gradle modules.

**Performance Goals**: Render the initial loading state immediately; show the product list or terminal response state as soon as the request completes. No additional numeric network or rendering latency target is specified.

**Constraints**: Internet access is required. No offline caching. Ktor engines are target-specific (OkHttp on Android, Darwin on iOS); shared code remains platform-neutral. Coil's Ktor 3 network fetcher must be available to the shared `AsyncImage` on both targets. The Android host enables edge-to-edge; Compose content must honor system-bar and safe-area insets, including the iOS host's safe areas. Network failures become `Result` failures; cancellation must propagate. Keep unknown JSON fields ignorable. All versions must be declared in `gradle/libs.versions.toml`.

**Scale/Scope**: One unfiltered catalog request and one scrollable product list. No details, search, cart, pagination, or persistence. FakeStoreAPI product records contain the six requested fields; responses may also include extra fields such as `rating`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Before Phase 0

- **I. Shared-first multiplatform**: PASS — screen, ViewModel, domain, and networking abstractions are planned in `commonMain`; only engine selection is platform-specific.
- **II. Thin platform shells and SDK isolation**: PASS — platform entry points continue to host `App()` only; no vendor SDK is introduced.
- **III. MVVM and unidirectional flow**: PASS — one sealed `StateFlow<ProductsUiState>` is owned by the ViewModel; screen callbacks call `load()`/`refresh()`.
- **IV. Layered architecture**: PASS — presentation depends on domain contracts/use case only; DTOs and Ktor remain in data.
- **V. Test-first and fakes**: PASS — implementation tasks must begin with failing common tests, use a hand-written fake for domain/presentation tests, and cover repository success/error and state transitions.
- **VI. Cross-platform verification**: PASS — plan includes Android host tests, iOS simulator tests, Android debug build, and iOS app build.
- **VII. Simplicity and version catalog**: PASS — keep the existing Gradle modules; declare added dependency versions centrally; do not introduce persistence or unrelated abstractions.

### After Phase 1

- **I–II**: PASS — the target engine factory is isolated to `androidMain`/`iosMain`; common API and UI contain no platform types.
- **III**: PASS — `ProductsViewModel` exposes exactly one sealed state flow. `Success` and `Empty` may carry refresh progress/failure metadata so failed refreshes preserve content while satisfying the spec.
- **IV**: PASS — `ProductApi`, `ProductDto`, mapping, and repository implementation remain in data; `Product`, repository interface, and use case remain in domain.
- **V**: PASS — test plan covers use-case, ViewModel, DTO mapping, and repository request/error behavior with fakes or MockEngine, not JVM-only mocks.
- **VI**: PASS — shared source and tests are verified on both targets and the app build targets are included in quickstart.
- **VII**: PASS — no extra Gradle modules, storage, or nonessential product features are planned. Dependencies are centralized in the catalog.

## Project Structure

### Documentation (this feature)

```text
specs/002-browse-products/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── products-api.md
└── tasks.md                 # Created by /speckit-tasks
```

### Source Code (repository root)

```text
shared/src/commonMain/kotlin/rw/kmp/product/
├── data/
│   ├── di/DataModule.kt
│   ├── dto/ProductDto.kt
│   ├── network/ProductApi.kt
│   ├── network/HttpClientFactory.kt
│   ├── repository/ProductRepositoryImpl.kt
│   └── mapper/ProductMapper.kt
├── di/AppModules.kt
├── domain/
│   ├── di/DomainModule.kt
│   ├── model/Product.kt
│   ├── repository/ProductRepository.kt
│   └── usecase/GetProductsUseCase.kt
└── presentation/products/
    ├── di/ProductsPresentationModule.kt
    ├── ProductsScreen.kt
    ├── ProductsUiState.kt
    └── ProductsViewModel.kt

shared/src/androidMain/kotlin/rw/kmp/product/
└── data/network/PlatformHttpClientEngine.android.kt

shared/src/iosMain/kotlin/rw/kmp/product/
└── data/network/PlatformHttpClientEngine.ios.kt

shared/src/commonTest/kotlin/rw/kmp/product/
├── data/                 # DTO mapper and MockEngine repository tests
├── domain/               # GetProductsUseCase tests and FakeProductRepository
└── presentation/         # ProductsViewModel state-transition tests (Turbine)

androidApp/src/main/AndroidManifest.xml  # INTERNET permission
androidApp/src/main/kotlin/rw/kmp/product/MainActivity.kt  # Android edge-to-edge host
gradle/libs.versions.toml                # Centralized dependency/plugin versions
shared/build.gradle.kts                  # Source-set dependencies
```

**Structure Decision**: Follow the existing `:shared` module and `rw.kmp.product` namespace. Organize by the three constitution-mandated layers with one Koin module for each layer. Platform source sets provide only the matching Ktor engine. `App()` composes the layer modules into the shared Koin application, and the screen obtains its ViewModel from Koin.

## Design Decisions

- Use `Product.id` as the stable lazy-list key.
- Use a Material 3 top app bar titled "Products"; ensure shared screen content observes platform insets so the app bar and list remain clear of system bars and iOS safe areas.
- Use the response fields `id`, `title`, `price`, `description`, `category`, and `image`; ignore unrelated fields such as the observed `rating` object.
- Treat FakeStoreAPI prices as USD and format every price with a consistent USD symbol and two decimal places. The endpoint payload has no currency field.
- Use `Loading`, `Success`, `Empty`, and `Error` for screen states. Model refresh status/failure as metadata on `Success`/`Empty`, preserving existing content if refresh fails; reserve `Error` for a failed initial load with retry.
- `load()` performs initial loading and retry; `refresh()` performs refresh semantics. Both call the same use case, but preserve their distinct UI behavior.
- Build one shared `HttpClient` in the data Koin module using a platform-provided engine and a shared `Json` configuration with `ignoreUnknownKeys = true`.
- Include Coil's Ktor 3 network artifact so `AsyncImage` can fetch remote URLs, using the existing OkHttp and Darwin Ktor engines on their respective targets.
- Translate HTTP/decoding failures into failed `Result`s in the data layer. Catch cancellation only to rethrow it; do not wrap suspending calls in `runCatching`.

## Complexity Tracking

No constitution violations or additional project/module complexity are introduced.
