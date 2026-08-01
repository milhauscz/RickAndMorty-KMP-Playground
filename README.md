![Rick and Morty KMP playground app](./docs/images/Rick_And_Morty_KMP_banner.png)

# Rick & Morty

A Kotlin Multiplatform (Android + iOS) app for browsing characters from the
[Rick and Morty API](https://rickandmortyapi.com/), built with Compose Multiplatform
and a fully modularized, offline-first architecture.

This repository is mirrored on [GitLab](https://gitlab.com/milhauscz-mobile/RickAndMorty-KMP-Playground)
(CI, Package Registry) and [GitHub](https://github.com/milhauscz/RickAndMorty-KMP-Playground).

> **Branch note.** This repository on `feature/sdk-showcase`  is the
> **SDK distribution** of the project: feature modules ship as published Maven artifacts and an
> XCFramework. The **playground app only** — no SDK packaging — lives on `main`.

[[_TOC_]]

## Screenshots

![Playground app animation demo](./docs/images/app-demo.gif) ![Characters list](./docs/screenshots/list.png) ![Filters](./docs/screenshots/filters.png) ![Character Detail](./docs/screenshots/detail.png)

Adaptive two-pane list/detail layout on expanded-width windows (tablets, landscape):

![Two/pane list/detail](./docs/screenshots/two-pane.png)

## Features

- **Character list** with endless scrolling backed by Paging 3 and a `RemoteMediator`,
  so pages are fetched from the network, cached in a local database, and served from there.
  Pull-to-refresh forces a fresh fetch, bypassing the HTTP cache.
- **Filtering** by name, species, type, status, and gender. Active filters show as
  dismissable chips with a *Clear all* shortcut; filter state is persisted in the database.
- **Character detail** screen with a collapsing hero image, status/species/gender cards,
  origin & current-location details, and an episode carousel.
- **Adaptive two-pane layout**: on expanded-width windows (tablets, landscape) the list and
  detail are shown side by side; on compact widths the detail is a separate screen with a
  shared-element image transition. In that single-pane mode, swiping left/right on the detail
  switches between characters, keeping the selection in sync with the list.
- **Offline-first**: the Room database is the single source of truth, so previously loaded
  content is available without a network connection.
- **Shared UI codebase** across Android and iOS via Compose Multiplatform.

## Architecture

The app follows a modularized, Now-in-Android-style structure with a clean
`data → domain → ui` layering inside each feature and unidirectional MVVM in the UI layer.

### Module graph

Direct Gradle dependencies (`A → B` means `:A` depends on `:B`). **Source of truth:** [`docs/images/module-deps.json`](./docs/images/module-deps.json). Edit that file, then run `.\scripts\render-module-graphs.ps1` to regenerate PNGs and the [dependency table](./docs/images/module-deps.md). Graphs use straight arrows (Mermaid ELK + `curve: linear`), with direct edges and no merge buses.

**Overview** — apps, demo umbrella, runtime, and feature modules (no `:core:*`):

<p align="center">
  <img src="./docs/images/module-graph-overview.png" width="700" alt="High-level module graph" />
</p>

**Full graph** — all modules: [module-graph.png](./docs/images/module-graph.png)

**Per core module** — who depends on each `:core:*` module:

`:core:common`

<p align="center">
  <img src="./docs/images/module-graph-core-common.png" width="700" alt="Dependents of :core:common" />
</p>

`:core:network`

<p align="center">
  <img src="./docs/images/module-graph-core-network.png" width="600" alt="Dependents of :core:network" />
</p>

`:core:database`

<p align="center">
  <img src="./docs/images/module-graph-core-database.png" width="600" alt="Dependents of :core:database" />
</p>

`:core:designsystem`

<p align="center">
  <img src="./docs/images/module-graph-core-designsystem.png" width="500" alt="Dependents of :core:designsystem" />
</p>

`:core:featureflags`

<p align="center">
  <img src="./docs/images/module-graph-core-featureflags.png" width="500" alt="Dependents of :core:featureflags" />
</p>

`:core:image`

<p align="center">
  <img src="./docs/images/module-graph-core-image.png" width="500" alt="Dependents of :core:image" />
</p>

`:iosApp` is an Xcode target (not a Gradle module) that links the `:shared` framework — shown with a dashed arrow.

Each feature is split into **api** (domain contract), **impl** (data + use cases), and — where
applicable — **ui** (Compose screens and ViewModels). Cross-feature dependencies use **api**
modules only at compile time.

| Module | Responsibility |
| --- | --- |
| `:androidApp` | Thin Android entry point (`MainActivity`, manifest). |
| `:shared` | Demo umbrella: `App` composable, navigation, iOS framework. Not published. |
| `:runtime` | Published entry point: `RickAndMortySdk.initialize`, config, isolated Koin, `RickAndMortySdkScope`. |
| `:feature:characters:api` | Domain models and `CharactersRepository`. |
| `:feature:characters:impl` | Data layer, use cases, repository impl (no Compose). |
| `:feature:characters:ui` | Published widget: list, detail, filters screens. |
| `:feature:episode:api` / `:feature:location:api` | Domain models and repository interfaces. |
| `:feature:episode:impl` / `:feature:location:impl` | Repository implementations and Koin modules. |
| `:core:common` | `Result`/`DataError`, shared models, annotations. |
| `:core:network` | Ktor client, `safeCall`, network Koin module. |
| `:core:database` | Room database and Koin module (KSP runs only here). |
| `:core:designsystem` | Material 3 theme and Compose resources. |
| `:core:image` | Coil image loader. |
| `:core:featureflags` | Remote config, rollout bucketing, host overrides. |
| `:konsist` | Architecture tests (layer, api/impl/ui rules). |
| `build-logic` | Gradle convention plugins. |

### Build logic (convention plugins)

- `rickandmorty.kmp.library` — KMP targets, host tests, lint, publishing metadata.
- `rickandmorty.kmp.feature` — the above plus Compose, Koin, lifecycle.
- `rickandmorty.kmp.published` — `explicitApi()` and ABI validation.
- `rickandmorty.publish` — Maven coordinates and registry wiring.
- `rickandmorty.compose` / `rickandmorty.room` / `rickandmorty.lint`.

### Data flow

```
UI (Compose screen)
  → ViewModel (StateFlow / Paging flow)
    → UseCase
      → Repository (interface in domain, impl in data)
        → Remote data source (Ktor)  → API
        └ Local data source (Room DAO) → SQLite   ◄── single source of truth
```

## The SDK

This branch ships the same features as **published libraries** — there is no `:sdk` facade.
Domain models and `Result<D, DataError>` are the public vocabulary.

| Integration | Gradle coordinates (see [publishing.md](docs/publishing.md)) |
| --- | --- |
| **Headless** (data only) | `:runtime` + `:feature:characters:impl` |
| **Widget** (screens + data) | `:runtime` + `:feature:characters:ui` |

`:shared` is the live demo: it calls `RickAndMortySdk.initialize(...)` and renders character
screens inside `RickAndMortySdkScope`.

### Consuming the SDK

Add the GitLab Package Registry (or your mirror) and depend on the artifacts:

```kotlin
repositories {
    maven("https://gitlab.com/api/v4/projects/<project-id>/packages/maven")
}

dependencies {
    // Headless
    implementation("cz.cernilovsky.kmp.rickandmorty:runtime:0.1.0")
    implementation("cz.cernilovsky.kmp.rickandmorty.feature.characters:impl:0.1.0")

    // Or widget (brings impl transitively)
    implementation("cz.cernilovsky.kmp.rickandmorty.feature.characters:ui:0.1.0")
}
```

Initialize once at startup. Pass `charactersUiModule` when using the widget:

```kotlin
import cz.cernilovsky.kmp.rickandmorty.characters.di.charactersUiModule
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdk
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdkScope
import cz.cernilovsky.kmp.rickandmorty.runtime.initialize

RickAndMortySdk.initialize(
    context = applicationContext,
    extraModules = listOf(charactersUiModule),
)

RickAndMortySdkScope {
    CharacterListDetailScreen(onFilterClick = { /* ... */ })
}
```

**Requirements:** Kotlin 2.4.0+, Android minSdk 24 / compileSdk 37, JVM 11+. iOS consumers use the
XCFramework — see [ios-integration.md](docs/ios-integration.md).

Verify locally before publishing:

```bash
./gradlew publishAllPublicationsToLocalTestRepository
```

### Documentation

| Document | Covers |
| --- | --- |
| [docs/publishing.md](docs/publishing.md) | Coordinates, versioning, local and CI publishing. |
| [docs/api-compatibility.md](docs/api-compatibility.md) | Public API policy and semver rules. |
| [docs/feature-flags.md](docs/feature-flags.md) | Remote config, overrides, rollout bucketing. |
| [docs/ios-integration.md](docs/ios-integration.md) | XCFramework and Swift Package manifest. |
| [docs/ci-components.md](docs/ci-components.md) | Reusable GitLab CI components. |
| [docs/sbom.md](docs/sbom.md) | CycloneDX bill of materials. |
| [docs/gitlab-mirror.md](docs/gitlab-mirror.md) | Mirroring to GitLab for CI and packages. |
| [CHANGELOG.md](CHANGELOG.md) | Per-release notes for SDK consumers. |

## Tech stack

| Concern | Library |
| --- | --- |
| UI | Compose Multiplatform, Material 3 |
| DI | Koin |
| Networking | Ktor client (kotlinx.serialization) |
| Persistence | Room + SQLite (bundled driver) |
| Paging | AndroidX Paging 3 (with `RemoteMediator`) |
| Images | Coil 3 (Ktor fetcher) |
| Navigation | Compose Navigation (type-safe routes) |
| Async | Kotlin Coroutines / Flow |
| Quality | kotlinter, detekt, Konsist |
| Build | Gradle convention plugins, version catalog |

## Building & running

Requirements: JDK 17+, the Android SDK (compileSdk 37), and — for iOS — Xcode on macOS.

### Android

```bash
./gradlew :androidApp:installDebug
```

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode, or build the shared framework:

```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

### iOS SDK binary (for integrators)

```bash
./gradlew :runtime:assembleRickAndMortySDKReleaseXCFramework
```

## Testing

```bash
./gradlew testAndroidHostTest
./gradlew qualityCheck
```

## Code quality

```bash
./gradlew formatKotlin
./gradlew lintKotlin detekt
./gradlew konsistCheck
```

[Konsist](https://docs.konsist.lemonappdev.com/) enforces layer boundaries, api/impl/ui rules, and
naming conventions. See [docs/port-konsist-setup.md](docs/port-konsist-setup.md) for porting the
setup to another project.
