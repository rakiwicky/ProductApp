# Tasks: Product Search

**Input**: Design documents from `/specs/004-product-search/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: The examples below include focused verification tasks where they add value, but they are optional unless the implementation explicitly requires TDD.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the existing KMP product catalog screen and shared model contracts are ready for search work.

- [x] T001 Review the current product catalog flow in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsScreen.kt` and `ProductsViewModel.kt` to confirm the search feature is implemented in the shared layer without platform-specific code.
- [x] T002 [P] Audit the product domain contract in `shared/src/commonMain/kotlin/rw/kmp/product/domain/model/Product.kt` and the product list state model in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsUiState.kt` so search state can be added without breaking the screen contract.
- [x] T003 [P] Check the existing shared test setup in `shared/build.gradle.kts` and `shared/src/commonTest/` to determine whether search verification will be added as UI/state tests or as a minimal regression suite.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared filtering behavior and search state that all product-search work depends on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T004 Define the search-aware UI state contract in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsUiState.kt` so the screen can distinguish idle, filtered, empty, and refresh states while keeping the existing catalog data intact.
- [x] T005 [P] Implement shared query normalization and filtering logic in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsViewModel.kt` for `title`, `category`, and `price` matching using case-insensitive comparison and whitespace trimming.
- [x] T006 [P] Add the reusable product filter behavior to the stored catalog state in `ProductsViewModel.kt` so the full product list is restored when the query is blank and the current catalog remains available for re-filtering.

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel.

---

## Phase 3: User Story 1 - Search the catalog by key product details (Priority: P1) 🎯 MVP

**Goal**: Filter the visible product list immediately by product name, category, or price without reloading the catalog.

**Independent Test**: Open the product list, type a query that matches a product title, category, or price, and verify the visible list updates immediately; clear the text and confirm the full catalog returns.

### Tests for User Story 1 (OPTIONAL - only if tests requested) ⚠️

- [x] T007 [P] [US1] Add a view-model regression test in `shared/src/commonTest/kotlin/rw/kmp/product/presentation/` covering name, category, and price matches with a blank-query reset.
- [x] T008 [P] [US1] Add a search-state test in `shared/src/commonTest/kotlin/rw/kmp/product/presentation/` confirming the filtered result set and restored catalog state behave correctly for text changes.

### Implementation for User Story 1

- [x] T009 [P] [US1] Add the search input field and real-time text handling to `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsScreen.kt` so the product list responds immediately as the user types.
- [x] T010 [US1] Wire `onSearchChanged` updates in `ProductsScreen.kt` to the shared `ProductsViewModel` state so the list and empty-state UI reflect the current query without disrupting the catalog refresh flow.
- [x] T011 [US1] Ensure the product list preserves loaded data while filtering in `ProductsViewModel.kt`, including the requirement that a blank or whitespace-only query restores the full available catalog.
- [x] T012 [US1] Validate price matching against the displayed numeric value in `ProductsViewModel.kt` so a search by whole or decimal price resolves against the existing product price data.

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently.

---

## Phase 4: User Story 2 - Handle results and no-match states (Priority: P2)

**Goal**: Show a clear no-results state and preserve the current search state when a query has no matches so the shopper understands the catalog is filtered rather than broken.

**Independent Test**: Search for a term with no matches and verify the empty-state message is visible while the query remains in the field; clear the query and confirm the full catalog returns without reloading.

### Tests for User Story 2 (OPTIONAL - only if tests requested) ⚠️

- [x] T013 [P] [US2] Add an empty-state regression test in `shared/src/commonTest/kotlin/rw/kmp/product/presentation/` for a query with no matches, keeping the search field value intact.
- [x] T014 [P] [US2] Add a validation test in `shared/src/commonTest/kotlin/rw/kmp/product/presentation/` ensuring clearing the query restores the catalog and removes the no-results state.

### Implementation for User Story 2

- [x] T015 [P] [US2] Update the empty-state rendering in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsScreen.kt` to display a clear message when the filtered list is empty while preserving search input and existing catalog data.
- [x] T016 [US2] Ensure the view model produces the correct empty-state transition in `ProductsViewModel.kt` when a query yields zero matches, while keeping `searchQuery` in the state for later clear/retry actions.
- [x] T017 [US2] Confirm clear-search behavior in `ProductsViewModel.kt` restores the full catalog immediately and does not require a reload or a refresh cycle.

**Checkpoint**: At this point, User Stories 1 and 2 should both work independently.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Final verification that the search feature aligns with the functional requirements and shared app behavior.

- [x] T018 [P] Review the product-search behavior against `specs/004-product-search/spec.md` to confirm the required cases for empty search, case-insensitive matching, empty-state messaging, and immediate updates are all covered.
- [x] T019 [P] Validate the live interaction in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsScreen.kt` to ensure the search field does not interfere with existing product detail navigation, refresh behavior, or catalog load/error states.
- [x] T020 Final regression check across the shared product list flow in `shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/` and `shared/src/commonTest/kotlin/rw/kmp/product/presentation/` so the feature remains independently testable and easy to hand off for implementation.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion - blocks all user-story work.
- **User Stories (Phase 3 and Phase 4)**: Depend on Foundational completion.
- **Polish (Phase 5)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2; no dependency on User Story 2.
- **User Story 2 (P2)**: Can start after Phase 2; should remain independently testable and can be implemented in parallel with US1 if desired.

### Within Each User Story

- state/filter logic before screen rendering
- query normalization before UI wiring
- empty-state UX before final validation
- story complete before moving to final polish

### Parallel Opportunities

- Phase 1 tasks can run in parallel because they are different review surfaces and do not modify the same files.
- The Foundation tasks in Phase 2 can proceed in parallel if the work is split across the state model and the view-model logic.
- User Story 1 implementation can be split between the UI search field (`ProductsScreen.kt`) and the filtering logic (`ProductsViewModel.kt`) once the foundation is complete.
- User Story 2 tasks can run in parallel with the US1 verification tasks once the shared empty-state behavior is in place.

---

## Parallel Example: User Story 1

```bash
# Launch the UI and logic work together once the foundation is complete:
Task: "Add the search input field and real-time text handling in shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsScreen.kt"
Task: "Implement shared query normalization and filtering logic in shared/src/commonMain/kotlin/rw/kmp/product/presentation/products/ProductsViewModel.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (required before the feature can work)
3. Complete Phase 3: User Story 1 (search by name, category, and price)
4. Stop and validate the filtered list and clear-search behavior
5. Deploy or demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → shared filter contract is ready
2. Add User Story 1 → verify search updates immediately and clearing restores the catalog
3. Add User Story 2 → verify empty-state feedback and no-match handling
4. Finish final polish and cross-cutting validation
5. Each story adds value without breaking the previous story

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together.
2. Once Foundational is done:
   - Developer A: User Story 1 search/filter logic
   - Developer B: User Story 1 screen input and empty-state wiring
   - Developer C: User Story 2 no-results behavior and validation
3. Final polish is performed as a shared validation pass.

---

## Notes

- [P] tasks = different files, no dependencies, or independent review/verification work.
- [Story] labels map task ownership to specific user-story execution and traceability.
- Each story should be independently completable and testable.
- Search behavior should be local to the current catalog and not require a new network call.
- Clear-search and no-results cases must preserve the current product dataset and only update the visible list.
- Avoid: vague tasks, file conflicts, and cross-story dependencies that make the feature hard to validate independently.
