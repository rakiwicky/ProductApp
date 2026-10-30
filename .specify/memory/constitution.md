<!--
Sync Impact Report
- Version change: 1.0.0 → 1.1.0 (MINOR: new principles added, existing guidance materially
  expanded; no prior rule removed or weakened)
- Modified principles:
  - I. Shared-First Multiplatform Code → I. Shared-First Multiplatform Code (expanded: networking,
    no java.*/android.* in commonMain, kotlinx-datetime, Kermit)
  - II. Thin Platform Shells & Explicit Platform Boundaries → II. Thin Platform Shells, Platform
    Boundaries & Vendor SDK Isolation (expanded: vendor SDKs behind commonMain interfaces)
  - (new) III. MVVM & Unidirectional Data Flow
  - (new) IV. Layered Architecture
  - III. Test-First (NON-NEGOTIABLE) → V. Test-First & Fakes-Based Testing (NON-NEGOTIABLE)
    (expanded: Turbine, fakes over MockK, mandatory ViewModel/repository tests)
  - IV. Cross-Platform Verification → VI. Cross-Platform Verification (renumbered)
  - V. Simplicity & Centralized Dependencies → VII. Simplicity, Small Changes & Clean Boundaries
    (expanded: small focused changes, module boundaries)
- Added sections: none (Technology & Platform Constraints expanded with mandated stack:
  Ktor/OkHttp/Darwin, kotlinx.serialization, Koin, Coil 3, coroutines/Flow; web target planned)
- Removed sections: none
- Templates requiring updates: none edited. Dependent templates read the constitution at runtime
  (.specify/templates/plan-template.md, spec-template.md, tasks-template.md). Plans MUST now check
  Principles I–VII.
- Follow-up TODOs / migration notes:
  - Current codebase does not yet declare Ktor, kotlinx.serialization, kotlinx-datetime, Kermit,
    Koin, Coil 3, coroutines-test, or Turbine in gradle/libs.versions.toml; add them when the
    first feature requires them.
  - Existing template code (Greeting, Platform, App.kt) predates the layered/MVVM structure and
    MUST be migrated or removed when touched.
-->

# ProductApp Constitution

## Core Principles

### I. Shared-First Multiplatform Code

- UI, state management (ViewModels), networking, data models, and business logic MUST live in
  `shared/src/commonMain`. Platform source sets are used only where a platform API makes shared
  code impossible.
- UI MUST be built with Compose Multiplatform in `commonMain`. Platform-native UI
  (SwiftUI/Android Views) is permitted only when a documented platform need exists.
- `commonMain` MUST NOT import `java.*`, `javax.*`, or `android.*` APIs. Use multiplatform
  equivalents: `kotlinx-datetime` for dates/times, Kermit for logging.
- Code duplicated across `androidMain` and `iosMain` that could be expressed in `commonMain`
  is a defect and MUST be consolidated.

Rationale: One codebase for all platforms keeps behavior consistent, cuts maintenance cost, and
keeps the planned web target reachable.

### II. Thin Platform Shells, Platform Boundaries & Vendor SDK Isolation

- `androidApp` and `iosApp` MUST remain thin entry points (e.g., `MainActivity`,
  `MainViewController`, `ContentView`) with no business logic.
- Platform-specific behavior MUST be exposed through `expect`/`actual` declarations or
  interfaces defined in `commonMain` and implemented in `androidMain`/`iosMain`.
- Every vendor/third-party platform SDK (analytics, payments, auth, crash reporting, etc.)
  MUST sit behind an interface in `commonMain`, with per-platform implementations wired via DI.
  Shared code MUST depend only on the interface.
- Platform types MUST NOT leak into `commonMain` APIs.

Rationale: Clear boundaries keep shared code portable, swappable, and testable with fakes.

### III. MVVM & Unidirectional Data Flow

- Presentation follows MVVM using the multiplatform `androidx.lifecycle` `ViewModel`.
- Each screen's ViewModel MUST expose exactly one `StateFlow<UiState>`, where `UiState` is a
  sealed type (e.g., `Loading`, `Content`, `Error`). The backing `MutableStateFlow` MUST be
  private.
- Data flows one way: user events go from Composables to the ViewModel (via functions or a
  sealed event type), the ViewModel updates state, and Composables render state. Composables
  MUST NOT mutate state owned by a ViewModel directly.
- Screen Composables collect state lifecycle-aware (`collectAsStateWithLifecycle`); child
  Composables SHOULD be stateless and receive state and callbacks as parameters.

Rationale: A single, sealed, observable state per screen makes UI predictable and testable.

### IV. Layered Architecture

- Code is organized into three layers with dependencies pointing inward:
  - **data**: Ktor client, DTOs, mappers, repository implementations.
  - **domain**: domain models and repository interfaces (pure Kotlin, no framework deps).
  - **presentation**: ViewModels and Composables.
- `presentation` depends only on `domain`. It MUST NOT reference Ktor types or DTOs.
- `domain` MUST NOT depend on `data` or `presentation`.
- DTOs MUST be mapped to domain models inside the data layer; network/serialization errors
  MUST be translated into domain-level results or errors before leaving the data layer.

Rationale: Layer isolation keeps UI independent of transport details and lets each layer be
tested and replaced on its own.

### V. Test-First & Fakes-Based Testing (NON-NEGOTIABLE)

- For every new behavior, tests MUST be written first and MUST fail before the implementation
  exists (Red-Green-Refactor). Bug fixes MUST include a regression test.
- Shared tests live in `shared/src/commonTest` and use `kotlin.test`, `kotlinx-coroutines-test`,
  and Turbine for `Flow`/`StateFlow` assertions.
- Test doubles MUST be hand-written fakes implementing domain/platform interfaces. MockK and
  other JVM-only mocking libraries are prohibited in shared tests.
- Every ViewModel and every repository implementation MUST have unit tests covering success,
  error, and loading/state-transition paths.

Rationale: Fakes run on every target, keep tests honest about contracts, and catch regressions
on both platforms.

### VI. Cross-Platform Verification

- Changes to `commonMain` MUST pass tests on both targets:
  `./gradlew :shared:testAndroidHostTest` and `./gradlew :shared:iosSimulatorArm64Test`.
- Platform-specific `actual` implementations MUST have tests in the matching source set
  (`androidHostTest`, `iosTest`) where feasible.
- Every change MUST build and run on both `androidApp`
  (`./gradlew :androidApp:assembleDebug`) and `iosApp` (Xcode) before it is considered done.

Rationale: Code that compiles for one target can still fail or behave differently on the other.

### VII. Simplicity, Small Changes & Clean Boundaries

- Prefer small, focused changes that do one thing; unrelated refactors MUST be split out.
- Start with the simplest solution that meets the spec (YAGNI). New modules, layers, or
  abstractions beyond those defined here MUST be justified in the plan's Complexity Tracking
  section.
- Module and package boundaries MUST be explicit; feature code MUST NOT reach into another
  feature's internal implementation.
- All dependency and plugin versions MUST be declared in `gradle/libs.versions.toml` and
  referenced through the version catalog. Hard-coded versions in build scripts are prohibited.
- A library added to `commonMain` MUST support every shipped target (Android, iOS) and SHOULD
  support the planned web target.

Rationale: Small changes and clean boundaries keep reviews fast, builds reproducible, and the
codebase easy to evolve.

## Technology & Platform Constraints

- **Language**: Kotlin (version per version catalog) for all shared and Android code; Swift only
  for the iOS entry point and unavoidable native integration.
- **Targets**: Android (minSdk/compileSdk per version catalog) and iOS (`iosArm64`,
  `iosSimulatorArm64`). Web is planned; adding or removing a target requires a constitution
  amendment.
- **UI**: Compose Multiplatform with Material 3. Images MUST be loaded with Coil 3.
- **Networking**: Ktor client with the OkHttp engine on Android and the Darwin engine on iOS;
  engine selection lives in platform source sets or DI modules only.
- **Serialization**: kotlinx.serialization; the shared `Json` instance MUST set
  `ignoreUnknownKeys = true`.
- **Dependency Injection**: Koin (including Koin's Compose/ViewModel integration). Hilt, Dagger,
  and other JVM/Android-only DI frameworks are prohibited in shared code.
- **Async**: Kotlin coroutines and `Flow`. Code that catches `Exception`/`Throwable` around
  suspending calls MUST rethrow `CancellationException`; `runCatching` around suspend calls is
  prohibited unless it rethrows cancellation.
- **Utilities**: `kotlinx-datetime` for time, Kermit for logging.
- The shared module is exported to iOS as the static framework `Shared`; its public API MUST stay
  Swift-friendly (avoid exposing complex generics or Kotlin-only constructs at the boundary).
- **Package namespace**: `rw.kmp.product`.

## Development Workflow & Quality Gates

- Features follow the Spec Kit flow: `/speckit-specify` → `/speckit-clarify` (as needed) →
  `/speckit-plan` → `/speckit-tasks` → `/speckit-implement`.
- Every plan MUST include a Constitution Check confirming compliance with Principles I–VII.
- Before merge, all of the following MUST pass: shared common tests, Android host tests,
  iOS simulator tests, the Android debug build, and the iOS app build.
- Code review MUST verify: logic is in `commonMain`, no `java.*`/`android.*` imports in
  `commonMain`, layer rules are respected (no Ktor/DTOs in presentation), each screen exposes a
  single sealed `UiState`, tests precede implementation and use fakes, `CancellationException`
  is rethrown, and no hard-coded dependency versions were introduced.

## Governance

- This constitution supersedes all other project practices. Where guidance conflicts,
  the constitution wins.
- Amendments require a documented change to this file, a stated rationale, and a migration
  note for any existing code that no longer complies.
- Versioning follows semantic versioning: MAJOR for removed or redefined principles,
  MINOR for new principles or materially expanded guidance, PATCH for clarifications.
- All pull requests and reviews MUST verify compliance. Any deviation MUST be justified in
  the relevant plan's Complexity Tracking section.
- Runtime development guidance lives in `README.md`.

**Version**: 1.1.0 | **Ratified**: 2026-10-28 | **Last Amended**: 2026-10-28
