# Tasks: Browse Products

**Input**: Design documents from `/specs/002-browse-products/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/products-api.md`, and `quickstart.md`

**Tests**: Included because the feature request explicitly asks for use-case, DTO mapping, and ViewModel tests, and the project constitution requires test-first implementation.

**Organization**: Tasks are grouped by user story after shared setup and foundational platform wiring. Test tasks must be completed before the implementation tasks they cover.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel with another task in a different file without depending on its incomplete work.
- **[Story]**: User story this task supports.
- Each task names the exact source, test, configuration, or documentation paths.

## Phase 1: Setup

**Purpose**: Add the dependencies and platform access required by the implementation.

- [X] T001 Add version-catalog aliases and shared source-set dependencies for Ktor core/content negotiation/JSON/MockEngine, serialization plugin, OkHttp/Darwin engines, Koin core/Compose/ViewModel, Coil 3 Compose and its Ktor 3 network fetcher, kotlinx-coroutines-test, and Turbine in `gradle/libs.versions.toml` and `shared/build.gradle.kts`; apply the version-catalog serialization plugin to `shared/build.gradle.kts` and keep every version centralized and compatible with the existing Kotlin/Compose versions.
- [X] T002 [P] Add the Android `INTERNET` permission to `androidApp/src/main/AndroidManifest.xml`; do not add iOS permission configuration.

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Provide a platform-neutral way for shared networking to obtain each target's Ktor engine.

**⚠️ CRITICAL**: Complete these tasks before user-story data networking work.

- [X] T003 Declare the platform HTTP engine factory in `shared/src/commonMain/kotlin/rw/kmp/product/data/network/PlatformHttpClientEngine.kt` without exposing a platform engine type outside the data/network boundary.
- [X] T004 [P] Implement the Android HTTP engine factory with OkHttp in `shared/src/androidMain/kotlin/rw/kmp/product/data/network/PlatformHttpClientEngine.android.kt`.
- [X] T005 [P] Implement the iOS HTTP engine factory with Darwin in `shared/src/iosMain/kotlin/rw/kmp/product/data/network/PlatformHttpClientEngine.ios.kt`.

**Checkpoint**: Platform-specific HTTP engines can be selected without importing platform APIs into `commonMain`.

## Phase 3: User Story 1 - Browse the product catalog (Priority: P1) 🎯 MVP

**Goal**: Load and display catalog products with their image, title, category, and consistently formatted USD price.

**Independent Test**: With a successful non-empty catalog result, open the screen and verify every product displays its image or fallback, title (no more than two lines), category, and USD price; verify list identity uses the product ID.

### Tests for User Story 1

> Write tests first and confirm they fail before implementing the behavior.

- [X] T006 [P] [US1] Write `GetProductsUseCase` success and failure delegation tests and a hand-written, reusable `FakeProductRepository` in `shared/src/commonTest/kotlin/rw/kmp/product/domain/usecase/GetProductsUseCaseTest.kt`; keep the fake visible to other common test packages.
- [X] T007 [P] [US1] Write DTO-to-domain field mapping and Ktor MockEngine repository success, HTTP-failure, invalid-response, and cancellation tests in `shared/src/commonTest/kotlin/rw/kmp/product/data/repository/ProductRepositoryImplTest.kt`.
- [X] T008 [US1] Write initial Loading-to-Success ViewModel state-flow tests using Turbine and `FakeProductRepository` in `shared/src/commonTest/kotlin/rw/kmp/product/presentation/products/ProductsViewModelTest.kt`; also write fixed two-decimal USD formatter tests in `shared/src/commonTest/kotlin/rw/kmp/product/presentation/products/ProductPriceFormatterTest.kt`.

### Implementation for User Story 1

- [X] T009 [US1] Define `Product` with required `id: Int`, `title: String`, `price: Double`, `description: String`, `category: String`, and `imageUrl: String` fields, and declare `ProductRepository.getProducts(): Result<List<Product>>` in `shared/src/commonMain/kotlin/rw/kmp/product/domain/model/Product.kt` and `shared/src/commonMain/kotlin/rw/kmp/product/domain/repository/ProductRepository.kt`.
- [X] T010 [US1] Implement `GetProductsUseCase` as a domain-only delegation to `ProductRepository` in `shared/src/commonMain/kotlin/rw/kmp/product/domain/usecase/GetProductsUseCase.kt`.
- [X] T011 [US1] Define `@Serializable ProductDto` with the six API fields (`id`, `title`, `price`, `description`, `category`, and `image`) and map `image` to `Product.imageUrl` in `shared/src/commonMain/kotlin/rw/kmp/product/data/dto/ProductDto.kt` and `shared/src/commonMain/kotlin/rw/kmp/product/data/mapper/ProductMapper.kt`.
- [X] T012 [US1] Implement `ProductApi` to request `GET https://fakestoreapi.com/products`, decode the JSON array using the shared `Json` configuration with `ignoreUnknownKeys = true`, and reject non-success responses in `shared/src/commonMain/kotlin/rw/kmp/product/data/network/ProductApi.kt` and `shared/src/commonMain/kotlin/rw/kmp/product/data/network/HttpClientFactory.kt`.
- [X] T013 [US1] Implement `ProductRepositoryImpl` to map DTOs to domain products and return transport/decoding failures as failed `Result`s while rethrowing `CancellationException` in `shared/src/commonMain/kotlin/rw/kmp/product/data/repository/ProductRepositoryImpl.kt`.
- [X] T014 [US1] Implement the single `ProductsUiState` flow and `ProductsViewModel.load()` initial Loading-to-Success path using only `GetProductsUseCase` in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsUiState.kt` and `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsViewModel.kt`.
- [X] T015 [US1] Implement the non-empty catalog screen with a stable-key `LazyColumn` keyed by product ID, Coil `AsyncImage` with the Ktor 3 network fetcher and a neutral unavailable-image fallback, two-line title truncation, category, initial `load()` callback, and formatted USD prices in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsScreen.kt`; implement deterministic USD formatting to two decimal places without JVM-only APIs in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductPriceFormatter.kt`.
- [X] T016 [US1] Register the domain use case, shared Ktor client and data repository, and Products ViewModel in one Koin module per layer in `shared/src/commonMain/kotlin/rw/kmp/product/domain/di/DomainModule.kt`, `shared/src/commonMain/kotlin/rw/kmp/product/data/di/DataModule.kt`, and `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/di/ProductsPresentationModule.kt`.
- [X] T017 [US1] Compose the three layer modules at the `App()` root, start the Koin Compose application with those modules, and render `ProductsScreen` in `shared/src/commonMain/kotlin/rw/kmp/product/di/AppModules.kt` and `shared/src/commonMain/kotlin/rw/kmp/product/App.kt`.
- [X] T018 [US1] Add a Material 3 top app bar titled "Products" using shared Compose string resources carried by `ProductsUiState` and covered by ViewModel tests; ensure the shared screen applies correct safe-area/system-bar insets on Android and iOS; retain or adjust Android `enableEdgeToEdge()` in `androidApp/src/main/kotlin/rw/kmp/product/MainActivity.kt` and verify the Compose iOS host in `shared/src/iosMain/kotlin/rw/kmp/product/MainViewController.kt`, implementing the shared layout in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsScreen.kt`, state in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsUiState.kt`, and strings in `shared/src/commonMain/composeResources/values/strings.xml`.

**Checkpoint**: A successful non-empty response is independently browsable with all product fields, stable item identity, image fallback, title truncation, and USD display.

## Phase 4: User Story 2 - Understand loading and empty results (Priority: P2)

**Goal**: Show unambiguous initial loading, empty-catalog, and initial-error states, with retry after an initial failure.

**Independent Test**: Exercise delayed, empty, and failed initial repository results; verify Loading while pending, Empty after an empty success, and Error with a working retry after failure.

### Tests for User Story 2

> Write tests first and confirm they fail before implementing the behavior.

- [ ] T019 [US2] Add Turbine ViewModel tests for successful empty results, initial request failure, and retry succeeding to products or empty state in `shared/src/commonTest/kotlin/rw/kmp/product/presentation/products/ProductsViewModelTest.kt`.

### Implementation for User Story 2

- [ ] T020 [US2] Extend `ProductsUiState` and `ProductsViewModel.load()` to emit Empty for a successful empty result and Error for initial failures while keeping retry on `load()` in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsUiState.kt` and `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsViewModel.kt`.
- [ ] T021 [US2] Render the initial Loading indicator, Empty message, and clear Error message with a retry action wired to `ProductsViewModel.load()` in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsScreen.kt`.

**Checkpoint**: Every initial request outcome is understandable and recoverable without changing the product-list behavior.

## Phase 5: User Story 3 - Refresh and recover the catalog (Priority: P2)

**Goal**: Let shoppers pull to refresh and retry refresh failures without losing products already visible.

**Independent Test**: From a non-empty Success state, trigger refresh and verify updated products on success; on refresh failure verify the existing products remain visible, the failure is communicated, and another refresh can be initiated.

### Tests for User Story 3

> Write tests first and confirm they fail before implementing the behavior.

- [ ] T022 [US3] Add Turbine ViewModel tests for refresh-in-progress, refresh-success replacing the current result, and refresh-failure preserving existing products with retryable feedback in `shared/src/commonTest/kotlin/rw/kmp/product/presentation/products/ProductsViewModelTest.kt`.

### Implementation for User Story 3

- [ ] T023 [US3] Implement `ProductsViewModel.refresh()` so Success/Empty carries refresh progress, refresh success updates to the latest Success/Empty result, and refresh failure preserves existing content with an error message in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsUiState.kt` and `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsViewModel.kt`.
- [ ] T024 [US3] Wrap the scrollable product content in Material 3 `PullToRefreshBox`, wire its refresh callback and progress state, and render refresh-failure feedback with a retry action calling `refresh()` without replacing visible products in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsScreen.kt`.

**Checkpoint**: Refresh updates content on success and preserves the last successful content on failure.

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verify shared behavior on both targets and validate the documented end-to-end scenarios.

- [ ] T025 Run `./gradlew :shared:testAndroidHostTest` and fix feature test failures in `shared/src/commonTest/kotlin/rw/kmp/product/`.
- [ ] T026 Run `./gradlew :shared:iosSimulatorArm64Test` and fix iOS compilation/test failures in `shared/src/commonMain/kotlin/rw/kmp/product/` and `shared/src/iosMain/kotlin/rw/kmp/product/`.
- [ ] T027 Run `./gradlew :androidApp:assembleDebug` and resolve feature integration or Android manifest failures in `shared/build.gradle.kts` and `androidApp/src/main/AndroidManifest.xml`.
- [ ] T028 Run `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` to validate the iOS framework and execute the available Android manual scenarios from `specs/002-browse-products/quickstart.md`; record that an end-to-end iOS app run requires an Xcode host project, which is not present in this repository.

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T001 and T002 can run in parallel.
- **Foundational (Phase 2)**: Depends on Setup; blocks user-story Ktor engine wiring. T004 and T005 can run in parallel after T003.
- **User Stories (Phase 3+)**: Depend on foundational platform engine setup.
- **Polish (Phase 6)**: Depends on the stories selected for delivery; cross-platform verification should follow all implemented feature work.

### User Story Dependencies

- **User Story 1 (P1)**: Starts after Phase 2; establishes the shared product, data, ViewModel, UI, and DI baseline and is the MVP.
- **User Story 2 (P2)**: Extends the `ProductsUiState`, `ProductsViewModel`, and `ProductsScreen` introduced by US1; complete US1 first.
- **User Story 3 (P2)**: Extends the loaded content states and screen from US1 and requires the complete initial/error behavior from US2; complete US1 and US2 first.

### Within Each User Story

- Complete the story's tests first and observe their failure before implementing the associated behavior.
- Keep domain models/interfaces independent from data and presentation.
- Finish DTO mapping and repository behavior before wiring the successful product list.
- Keep the story checkpoint independently verifiable before starting the next story.

### Parallel Opportunities

- **Setup**: T001 and T002 touch separate files.
- **Foundational**: T004 and T005 are independent target-specific actual implementations after T003.
- **US1 tests**: T006 and T007 use separate test files and can be authored in parallel after setup/foundation. T008 follows T006 because it reuses `FakeProductRepository`.
- **US1 implementation**: T009 establishes the domain model/interface before T010 and T011; T012 follows T011 and T003; T013 follows T010 and T012; UI/ViewModel/DI integration follows their required domain/data contracts.
- **US2 and US3**: Test authoring is separate from implementation, but both stories intentionally follow US1 to avoid conflicts in the shared screen and state-flow files.
- **Polish**: Run T025–T028 after implementation; do not make competing source fixes in parallel across target validation.

## Parallel Example: User Story 1

```text
After T001–T005:
Task: Write use-case/fake repository tests in shared/src/commonTest/kotlin/rw/kmp/product/domain/usecase/GetProductsUseCaseTest.kt (T006)
Task: Write DTO-mapping/repository MockEngine tests in shared/src/commonTest/kotlin/rw/kmp/product/data/repository/ProductRepositoryImplTest.kt (T007)
After T006 has created FakeProductRepository:
Task: Write initial ViewModel and USD formatter tests in shared/src/commonTest/kotlin/rw/kmp/product/presentation/products/ProductsViewModelTest.kt and shared/src/commonTest/kotlin/rw/kmp/product/presentation/products/ProductPriceFormatterTest.kt (T008)

After those tests are written:
Task: Implement domain model and repository contract in shared/src/commonMain/kotlin/rw/kmp/product/domain/ (T009)
Then implement DTO and mapper in shared/src/commonMain/kotlin/rw/kmp/product/data/dto/ProductDto.kt and shared/src/commonMain/kotlin/rw/kmp/product/data/mapper/ProductMapper.kt (T011)
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Setup and Foundational tasks.
2. Complete US1 tests and implementation.
3. Validate a non-empty remote result, list presentation, image fallback, title truncation, and USD price display.
4. Stop at the US1 checkpoint for an independently demonstrable catalog screen.

### Incremental Delivery

1. Add US2 initial loading/empty/error/retry behavior and validate each initial request outcome.
2. Add US3 pull-to-refresh, updated results, and refresh-failure retention.
3. Run cross-platform tests and Android build; validate manual Android/iOS scenarios.

## Notes

- Every task uses the required checkbox, sequential ID, optional `[P]`, story label for story phases, and an exact file path.
- Tests are required by the explicit feature request and the project's test-first constitution.
- Use hand-written fakes for domain and ViewModel tests; Ktor MockEngine is used for HTTP-level repository tests.
- Preserve `CancellationException`; do not use `runCatching` around suspending work without rethrowing cancellation.
- Keep added dependency versions in `gradle/libs.versions.toml`; do not add Gradle modules or offline persistence.
