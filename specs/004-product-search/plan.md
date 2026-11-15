# Implementation Plan: Product Search

**Branch**: `004-product-search` | **Date**: 2026-11-03 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/004-product-search/spec.md`

## Summary

Add a shared, local product-search experience to the existing product list screen in `shared/src/commonMain`. The user can type a query and immediately filter the loaded product catalog by title, category, or price; the clear action resets the field and restores the full list. The feature must also provide a clear no-results empty state while keeping refresh and navigation behavior intact.

## Technical Context

**Language/Version**: Kotlin Multiplatform; Kotlin version from the existing version catalog.

**Primary Dependencies**: Compose Multiplatform, Material 3, lifecycle state collection, existing product domain model and ViewModel patterns.

**Storage**: None. Search is local to the loaded in-memory catalog.

**Testing**: `kotlin.test`, `kotlinx-coroutines-test`, and shared ViewModel tests in `shared/src/commonTest`.

**Target Platform**: Android and iOS; shared behavior stays in `shared/src/commonMain`.

**Project Type**: Kotlin Multiplatform mobile app with a shared Compose UI and a shared product list screen.

**Performance Goals**: Search updates immediately as the user types; no network latency introduced.

**Constraints**: Local filtering only; no backend search endpoint; shared behavior must remain platform-neutral; clear button and empty state must be reflected in state and UI together.

**Scale/Scope**: One screen with a product list; filtered results update from the loaded catalog without a reload.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Before Phase 0

- **I. Shared-first multiplatform**: PASS — the search filter logic and UI live in `shared/src/commonMain`.
- **II. Thin platform shells and SDK isolation**: PASS — no additional platform-bound SDK is introduced.
- **III. MVVM and unidirectional flow**: PASS — the screen calls `onSearchChanged`, the ViewModel updates state, and the UI renders state.
- **IV. Layered architecture**: PASS — presentation logic remains in the screen/ViewModel layer; no data-layer change is required.
- **V. Test-first and fakes**: PASS — search behavior will be covered by shared tests before implementation.
- **VI. Cross-platform verification**: PASS — Android host and iOS simulator checks remain required.
- **VII. Simplicity and version catalog**: PASS — this is a small feature change with no new modules or dependency versions.

### After Phase 1

- **I–II**: PASS — no platform-specific logic leaks into `commonMain`; the clear-button behavior stays in shared UI state.
- **III**: PASS — one sealed `ProductsUiState` remains the source of truth for the screen.
- **IV**: PASS — no data-layer refactor or DTO changes are required for this screen-local feature.
- **V**: PASS — tests will verify filtering by title/category/price, empty-state behavior, and the clear action.
- **VI**: PASS — Android and iOS validation remain part of the feature acceptance path.
- **VII**: PASS — the feature remains focused on a small change in the existing product list screen.

## Project Structure

### Documentation (this feature)

```text
specs/004-product-search/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── search-ui-contract.md
├── spec.md
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
shared/src/commonMain/kotlin/rw/kmp/product/
├── domain/
│   └── model/Product.kt
├── presentation/products/
│   ├── ProductsScreen.kt
│   ├── ProductsUiState.kt
│   └── ProductsViewModel.kt
└── ...

shared/src/commonTest/kotlin/rw/kmp/product/presentation/
└── ProductsViewModelTest.kt
```

**Structure Decision**: Keep the feature in the existing shared product-list layer and add the behavioral contract in `ProductsUiState` and `ProductsViewModel`; no new modules are necessary.

## Complexity Tracking

No constitution violations or new module complexity are introduced. The work remains contained to the existing shared product list screen and its state.

