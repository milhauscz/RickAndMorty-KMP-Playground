# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Kotlin Multiplatform (Android + iOS) app browsing the [Rick and Morty API](https://rickandmortyapi.com/),
built with Compose Multiplatform. Modularized, offline-first (Room is the single source of truth),
Now-in-Android-style architecture. See `README.md` for the feature overview and module graph.

## Commands

Run Gradle with a plain `./gradlew` (on Windows `.\gradlew`) invocation — no `cd`, redirects, or pipes.

```bash
# Tests — unit + Robolectric/Compose UI, all on the JVM host, across every module
./gradlew testAndroidHostTest
./gradlew :feature:characters:testAndroidHostTest          # single module
./gradlew :feature:characters:testAndroidHostTest --tests "*CharactersViewModelTest*"   # single test

# Code quality
./gradlew formatKotlin          # auto-fix formatting
./gradlew lintKotlin detekt     # verify style (kotlinter + detekt)

# Android app
./gradlew :androidApp:installDebug

# iOS compilation check (simulator tests themselves need macOS)
./gradlew compileKotlinIosSimulatorArm64
./gradlew :shared:embedAndSignAppleFrameworkForXcode      # build framework for Xcode
```

Requirements: JDK 17+, Android SDK (compileSdk 37), Xcode for iOS.

### Verifying a change

A full `./gradlew build` fails on pre-existing detekt/kotlinter debt, not just your change. To verify
your own work, run the relevant module's `testAndroidHostTest` (add `-x lintKotlin -x detekt` if you
need `build` but want to skip the style gates).

## Architecture

### Modules

Every module's build script is minimal because shared setup lives in the `build-logic` included build
as precompiled convention plugins. To change how modules are built (Kotlin/Android targets, Compose,
Koin, Room, lint), edit the plugin in `build-logic/convention/src/main/kotlin/*.gradle.kts`, not the
individual modules.

- `rickandmorty.kmp.library` — KMP targets (Android + iosArm64 + iosSimulatorArm64), namespace, host test task, lint.
- `rickandmorty.kmp.feature` — the above + Compose, Koin, lifecycle (for UI features).
- `rickandmorty.compose` — Compose Multiplatform + per-module resources.
- `rickandmorty.room` — Room + KSP wiring. **KSP/Room runs only in `:core:database`.**
- `rickandmorty.lint` — kotlinter + detekt (detekt config at `config/detekt/detekt.yml`).

Android namespaces are derived from the module path (see `ProjectExtensions.kt`), e.g.
`:core:database` → `cz.cernilovsky.kmp.rickandmorty.core.database`. All code lives under the
`cz.cernilovsky.kmp.rickandmorty` package base.

`:shared` is the umbrella module: `App` composable, type-safe navigation `Routes`, the iOS framework,
and the `initKoin` aggregation that wires every module's Koin module together (`shared/.../di/Module.kt`).
`:feature:episode` and `:feature:location` are data-only features consumed by the character detail screen.

### Layering inside a feature

Each feature is split into `data / domain / ui` packages with unidirectional MVVM:

```
ui (Compose screen) → ViewModel (StateFlow / Paging flow) → UseCase
  → Repository (interface in domain, impl in data)
    → Remote data source (Ktor) → API
    └ Local data source (Room DAO) → SQLite   ◄── single source of truth
```

- Interfaces live in `domain` (e.g. `CharactersRepository`); implementations live in `data`, suffixed
  `Impl` (e.g. `CharactersRepositoryImpl`) or `KtorImpl` for the Ktor-backed remote data sources (e.g.
  `CharactersDataSourceKtorImpl`). Koin binds impl to interface (`... bind CharactersRepository::class`).
- The character list uses a Paging 3 `RemoteMediator`: the UI observes a `PagingSource` over Room while
  the mediator fetches from the network and writes into the database on demand.
- Each feature owns a `di/<Feature>Module.kt` Koin module; add it to `initKoin` in `:shared` when creating a new feature.

### Platform-specific code

`expect`/`actual` and platform Koin modules use filename suffixes: `NetworkModule.kt` (commonMain),
`NetworkModule.android.kt` (androidMain), `NetworkModule.ios.kt` (iosMain). Platform Koin modules are
suffixed `...PlatformModule` and wired separately in `initKoin`.

### Tests

- `commonTest` — pure unit tests, run on both JVM host and iOS.
- `androidHostTest` — Robolectric + Compose UI tests, JVM host only. Fakes/fixtures for shared use live
  in `commonTest` (e.g. `CharacterFixtures.kt`, `FakeRepositories.kt`).
