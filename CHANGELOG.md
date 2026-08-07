# Changelog

All notable changes to the published SDK artifacts are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[semantic versioning as defined for this project](docs/api-compatibility.md).

The section for the version in `gradle.properties` becomes the GitLab release notes verbatim, via
`scripts/changelog-section.sh`.

## [Unreleased]

## [0.3.0] - 2026-08-07

### Added

- Headless character paging via `CharactersRepository.loadCharacters` /
  `LoadCharactersUseCase` with `CharactersLoadType` (`Initial` / `Append` / `Prepend`) and
  `CharactersPageLoadResult` (page + `hasMore` / `hasPrevious`). Local-first: warm Room cache
  serves windows without network; remote fetch on miss or when Init finds stale / filter-changed
  cache (same 15‑minute rule as the paging remote mediator).
- iOS headless bridge: `CharactersIosBridge` in `:runtime` `iosMain`, annotated with
  KMP-NativeCoroutines **1.0.4** (`@NativeCoroutinesRefined` / `@ShouldRefineInSwift`).
- First-party Swift package facade: `RickAndMorty.initializeHeadless`, `CharactersClient`, and
  SPM product **RickAndMortySDK** wrapping binary **RickAndMortySDKCore** so hosts do not depend
  on NativeCoroutines directly ([`Package.swift`](Package.swift),
  [`swift/Sources/RickAndMortySDK`](swift/Sources/RickAndMortySDK)).
- Room window queries on `CharactersRoomDataSource` for headless page reads.

### Changed

- **Breaking (ABI):** `CharactersRepository` gains `loadCharacters(...)` (hosts that implement the
  interface must add the method).
- Documented Android vs iOS headless/widget Quick Start flows and iOS Compose UI hosting in
  [README.md](README.md) / [docs/ios-integration.md](docs/ios-integration.md).

### Notes for integrators

- **Android / JVM headless:** `RickAndMortySdk.initialize(...)` then `get<LoadCharactersUseCase>()`
  (or `GetCharactersUseCase` / other registered types).
- **iOS headless:** depend on the **RickAndMortySDK** Swift product, call
  `RickAndMorty.initializeHeadless(...)`, use `CharactersClient` only (not the refined
  `__CharactersIosBridge` APIs).
- **Widget:** unchanged — `initializeWidget` + public screens inside `RickAndMortySdkScope`.

## [0.2.0] - 2026-08-03

### Added

- `SdkMode` (`Headless` / `Widget`) on `RickAndMortySdkConfig` to declare how the host consumes the
  SDK.
- `RickAndMortySdk.initializeWidget(...)` in `:feature:characters:ui` (Android + iOS) — registers
  the character UI Koin module and requires `SdkMode.Widget` (fail-fast if the UI graph is missing).
- `RickAndMortySdk.get<T>()` for headless resolution of use cases and repositories after
  `initialize`.
- Room-backed cache for remote feature-flag config so cold starts can apply the last successful
  fetch (and kill switch / rollout) before a new refresh completes.
- Best-effort GitLab `ios` job (`templates/ios-build.yml`, `allow_failure: true`) for Apple compile
  / smoke when a macOS runner is available.

### Changed

- **Breaking (ABI):** Character ViewModels, UI models/state (`UiCharacter`, `UiCharacterDetail`,
  etc.), DI modules/markers (`SdkWidgetMarker`), and feature-flag machinery
  (`FeatureFlagsRepository`, `FeatureFlag`, `FeatureFlagsConfig`) are `@InternalRickAndMortyApi` —
  excluded from ABI dumps. Hosts configure flags via `RickAndMortySdkConfig` /
  `RickAndMortyFeatureFlags` only; widget hosts use public screens, not ViewModels.
- **Breaking (ABI):** Merged `@InternalRickAndMortyRuntimeApi` into `@InternalRickAndMortyApi`.
  `RickAndMortySdk.internalContainer` uses the unified annotation. SDK modules opt in via the
  Gradle compiler `optIn` flag.
- **Breaking:** Renamed the flag domain API to `FeatureFlagsRepository` / `FeatureFlagsRepositoryImpl`
  (then marked internal). Removed the separate in-memory `DefaultFeatureFlags` layer in favour of a
  single Flow-backed repository (`stateIn(Eagerly)` over Room + async refresh on construction).
- `RickAndMortyFeatureFlags.CHARACTER_DETAIL_AUTO_REFRESH` is now a `const val` string key (no
  dependency on the internal `FeatureFlag` type).
- Coroutine dispatchers (`Default`, `IO`, `Main`, `Main.immediate`) are bound in DI for SDK
  internals; not part of the supported public contract.

### Notes for integrators

- **Headless:** `RickAndMortySdk.initialize(...)` then
  `RickAndMortySdk.get<GetCharactersUseCase>()` (or any type registered by the SDK).
- **Widget:** prefer `RickAndMortySdk.initializeWidget(...)` — do not pass `charactersUiModule` via
  `extraModules` unless you have a custom UI graph. Public screens only; ViewModels and UI state
  types are not part of the contract.
- See [docs/api-compatibility.md](docs/api-compatibility.md) for the supported Headless + UI
  surface.

## [0.1.0] - 2026-07-31

First release shipping feature modules.

### Added

- `:runtime` with `RickAndMortySdk.initialize(...)` / `shutdown()`, isolated Koin, and
  `RickAndMortySdkScope` for Compose hosts.
- `RickAndMortySdkConfig` (builder), `RickAndMortyFeatureFlags`, and `@ExperimentalRickAndMortyApi`.
- Published feature modules: `:feature:characters:api`, `:feature:characters:impl`,
  `:feature:characters:ui`, and sibling episode/location modules.
- Domain models, `Result<D, DataError>`, repositories, and use cases as the public vocabulary
- `character_detail_auto_refresh` feature flag (off by default).
- An `XCFramework` for `iosArm64` and `iosSimulatorArm64`, plus a root `Package.swift` manifest.
- A CycloneDX SBOM for `:runtime`, generated by `:runtime:cyclonedxDirectBom`.

### Changed

- Split `:feature:characters:impl` into headless `impl` (data/domain) and `ui` (screens/ViewModels).
- `:shared` is the integration demo.

### Notes for integrators

- **Kotlin 2.4.0 or newer is required.** AGP 9's built-in Kotlin is 2.2.0, so an Android consumer
  must raise it deliberately.
- The SDK runs in an isolated Koin container. Pass `charactersUiModule` via `extraModules` when
  using the character screens.
- **Headless:** depend on `:runtime` + `:feature:characters:impl`. **Widget:** depend on
  `:feature:characters:ui` (brings `impl` transitively).

[Unreleased]: https://gitlab.com/cernilovsky/rick_and_morty/-/compare/v0.3.0...development
[0.3.0]: https://gitlab.com/cernilovsky/rick_and_morty/-/compare/v0.2.0...v0.3.0
[0.2.0]: https://gitlab.com/cernilovsky/rick_and_morty/-/compare/v0.1.0...v0.2.0
[0.1.0]: https://gitlab.com/cernilovsky/rick_and_morty/-/tags/v0.1.0
