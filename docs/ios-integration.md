# iOS integration

## Consuming the SDK

The SDK ships as `RickAndMortySDK.xcframework` (`iosArm64` + `iosSimulatorArm64`). Build it with
**macOS + Xcode** (Kotlin/Native Apple targets cannot be compiled on Linux CI):

```bash
./gradlew :runtime:assembleRickAndMortySDKReleaseXCFramework
```

The output lands in `runtime/build/XCFrameworks/release/`, where the root `Package.swift` points.

Local smoke check (also macOS):

```bash
./gradlew compileKotlinIosSimulatorArm64 :runtime:iosSimulatorArm64Test
```

## Headless from Swift

`SdkMode.Headless` (the default) is the Swift-friendly path. Initialize once at app start, then use
`CharactersIosBridge` for character data.

### 1. Add dependencies

- **RickAndMortySDK** — the XCFramework (see `Package.swift` or your release artifact).
- **[KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) 1.0.4** — Swift
  Concurrency helpers. In Xcode: **File → Add Packages…** and add
  `https://github.com/rickclephas/KMP-NativeCoroutines.git` at **exact version 1.0.4**. Link the
  **KMPNativeCoroutinesAsync** product.

Use the **same** NativeCoroutines version on Kotlin (1.0.4) and Swift (1.0.4).

### 2. Initialize the SDK

```swift
import RickAndMortySDK

RickAndMortySdkIosKt.initialize(
    config: RickAndMortySdkConfig.companion.builder()
        .mode(mode: .headless)
        .baseUrl(value: "https://rickandmortyapi.com/api")
        .build()
)
```

### 3. Use the headless bridge

`CharactersIosBridge` lives in `:runtime` `iosMain` and is annotated with KMP-NativeCoroutines so
`Flow` APIs become `AsyncSequence` and `suspend` functions become cancellable `async`/`await` from
Swift.

```swift
import RickAndMortySDK
import KMPNativeCoroutinesAsync

let bridge = CharactersIosBridge.companion.create()

Task {
    do {
        // Init: first page from Room when fresh, otherwise remote refresh
        guard case let .success(first) = await asyncResult(
            for: bridge.loadCharacters(
                loadType: .init,
                filters: CharacterFilters.companion.EMPTY,
                anchorCharacterId: nil
            )
        ) else { return }

        var characters = first.characters
        var hasMore = first.hasMore

        // Append while scrolling: pass the last shown character id
        while hasMore, let lastId = characters.last?.id {
            guard case let .success(next) = await asyncResult(
                for: bridge.loadCharacters(
                    loadType: .append,
                    filters: CharacterFilters.companion.EMPTY,
                    anchorCharacterId: KotlinInt(value: Int32(lastId.intValue))
                )
            ) else { break }
            characters.append(contentsOf: next.characters)
            hasMore = next.hasMore
        }
    } catch {
        // handle load errors
    }
}

// Detail screen
Task {
    for try await detail in asyncSequence(for: bridge.observeCharacterDetail(id: 1)) {
        // render CharacterDetail?
    }
}

bridge.close() // before RickAndMortySdk shutdown
```

### What the bridge exposes

| Bridge API | Swift consumption |
| --- | --- |
| `loadCharacters(loadType, filters, anchorCharacterId)` | `asyncResult(for:)` — `Init` / `Append` / `Prepend`; returns characters + `hasMore` / `hasPrevious` |
| `observeCharacterDetail(id)` | `asyncSequence(for:)` |
| `refreshCharacterDetail(id)` | `asyncFunction(for:)` |
| `observeFilters()` / `setFilters(...)` | observe + one-shot update |
| `observeSelectedCharacterId()` / `setSelectedCharacterId(...)` | two-pane selection |

### Limitations

- **List data is pull-based**, not a reactive `Flow`. Call `loadCharacters(.init, …)` for the first
  page (local when cache is fresh; remote when stale or filters changed). Use `.append` / `.prepend`
  with the edge character id; keep the accumulated list in SwiftUI state.
- Domain models (`Character`, `CharacterDetail`, filters, `Result`) are exported in the XCFramework.
  Prefer the bridge over calling raw `Flow` use cases from Swift.
- Requires **iOS 14+** (project minimum) and Swift Concurrency (iOS 13+ for Async; we target iOS 14).

Advanced Kotlin hosts can still resolve use cases directly after init:

```kotlin
val useCase = RickAndMortySdk.get<GetCharactersUseCase>()
```

## Compose Multiplatform UI (not SwiftUI)

Character screens live in `:feature:characters:ui` and are **Compose Multiplatform**, hosted from
Kotlin (for example `ComposeUIViewController` in the demo's `MainViewController`). They are not
SwiftUI wrappers.

For the widget path use `SdkMode.Widget` and `RickAndMortySdk.initializeWidget(...)` from the
characters UI artifact (registers the UI Koin module automatically). That entry point is for Kotlin /
CMP hosts, not for a pure Swift UI.

```kotlin
// Kotlin / ComposeUIViewController configure block
RickAndMortySdk.initializeWidget(
    config = RickAndMortySdkConfig.builder().mode(SdkMode.Widget).build(),
)
```

**Platform-specific `initialize`.** Android needs a `Context`; iOS headless does not.
`RickAndMortySdkIosKt.initialize(config:extraModules:)` is the Swift / headless entry.
`initializeWidget` is the CMP widget entry from `:feature:characters:ui`.

## Known gaps

- The XCFramework and `compileKotlinIosSimulatorArm64` only run on macOS.
- GitLab Free has no macOS SaaS runners; the `ios` CI job uses `allow_failure: true` until a Mac
  runner is available. See [ci-components.md](ci-components.md).
- ABI validation on Linux only keeps previously recorded Apple slices. See
  [api-compatibility.md](api-compatibility.md).
