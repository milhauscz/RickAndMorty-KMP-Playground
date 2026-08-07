# iOS integration

## Consuming the SDK

The SDK ships as `RickAndMortySDKCore.xcframework` (`iosArm64` + `iosSimulatorArm64`) plus a thin
**Swift package** that wraps it. Build the framework with **macOS + Xcode** (Kotlin/Native Apple
targets cannot be compiled on Linux CI):

```bash
./gradlew :runtime:assembleRickAndMortySDKCoreReleaseXCFramework
```

The output lands in `runtime/build/XCFrameworks/release/`, where the root [`Package.swift`](../Package.swift)
points. The Swift sources live under [`swift/Sources/RickAndMortySDK`](../swift/Sources/RickAndMortySDK).

Local smoke check (also macOS):

```bash
./gradlew compileKotlinIosSimulatorArm64 :runtime:iosSimulatorArm64Test
```

Swift package smoke check (after assembling the XCFramework above):

```bash
./scripts/verify-swift-package.sh
```

Use SPM’s `--triple` and `--sdk` flags — **not** `-Xswiftc -target`/`-sdk`. Passing those only
forwards flags to the compiler; SPM still resolves modules against the macOS sysroot, so the
iOS-only XCFramework slice is not found.

The SPM binary target name (`RickAndMortySDKCore`) must match the Kotlin framework `baseName` and
the module inside the XCFramework.

```bash
# equivalent to the script
SDKROOT="$(xcrun --sdk iphonesimulator --show-sdk-path)"
swift build --triple arm64-apple-ios14.0-simulator --sdk "$SDKROOT"
```

## Headless from Swift

`SdkMode.Headless` (the default) is the Swift-friendly path. Initialize once at app start, then use
`CharactersClient` from the **RickAndMortySDK** Swift product.

### 1. Add the package

Add this repository (or the released package URL) as a Swift package dependency and link the
**RickAndMortySDK** product.

You do **not** need to add [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines)
yourself — the Swift package depends on exact **1.0.4** internally. Host apps only import
`RickAndMortySDK`.

Local development also requires building the XCFramework first (command above) so the binary target
path resolves.

### 2. Initialize the SDK

```swift
import RickAndMortySDK

RickAndMorty.initializeHeadless(
    baseUrl: "https://rickandmortyapi.com/api"
)
```

### 3. Use `CharactersClient`

```swift
import RickAndMortySDK

let client = CharactersClient()

Task {
    do {
        // Initial: first page from Room when fresh, otherwise remote refresh
        let first = try await client.loadCharacters(load: .initial)

        var characters = first.characters
        var hasMore = first.hasMore

        // Append while scrolling: pass the last shown character id
        while hasMore, let lastId = characters.last?.id {
            let next = try await client.loadCharacters(
                load: .append,
                anchorCharacterId: Int(lastId)
            )
            characters.append(contentsOf: next.characters)
            hasMore = next.hasMore
        }
    } catch {
        // RickAndMortySDKError.remote(...) or cancellation / transport errors
    }
}

// Detail screen
Task {
    for try await detail in client.characterDetail(id: 1) {
        // render CharacterDetail?
    }
}

client.close() // before RickAndMorty.shutdown()
RickAndMorty.shutdown()
```

### What the Swift facade exposes

| Swift API | Notes |
| --- | --- |
| `RickAndMorty.initializeHeadless(baseUrl:)` / `shutdown()` | Headless bootstrap |
| `CharactersClient.loadCharacters(load:filters:anchorCharacterId:)` | `.initial` / `.append` / `.prepend`; returns page + `hasMore` / `hasPrevious` |
| `CharactersClient.characterDetail(id:)` | `AsyncThrowingStream` of `CharacterDetail?` |
| `CharactersClient.refreshCharacterDetail(id:)` | One-shot remote refresh |
| `CharactersClient.filters()` / `setFilters(_:)` | Observe + update filters |
| `CharactersClient.selectedCharacterId()` / `setSelectedCharacterId(_:)` | Two-pane selection |
| `CharactersClient.close()` | Cancel bridge coroutines |

Domain models (`Character`, `CharacterDetail`, filters, etc.) come from the XCFramework and are
re-exported by the Swift module (`@_exported import RickAndMortySDKCore`).

### Package layout

| SPM target | Role |
| --- | --- |
| `RickAndMortySDKCore` | Binary XCFramework (Kotlin/Native) |
| `RickAndMortySDK` | Swift wrapper product hosts should link |

`CharactersIosBridge` is still in the XCFramework, but coroutine members are refined
(`@NativeCoroutinesRefined`) so they appear as `__…` and stay out of normal Swift autocomplete.
`create()` / `close()` remain visible but are not part of the supported host contract. The supported
Swift contract is `CharactersClient`. KMP-NativeCoroutines is an
implementation detail of the wrapper and can be replaced later without changing the host-facing API.

### Limitations

- **List data is pull-based**, not a reactive stream of the full cache. Call
  `loadCharacters(load: .initial)` for the first page (local when cache is fresh; remote when stale
  or filters changed). Use `.append` / `.prepend` with the edge character id; keep the accumulated
  list in SwiftUI state.
- Requires **iOS 14+** (project minimum) and Swift Concurrency.

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
`RickAndMorty.initializeHeadless` (or `RickAndMortySdk.shared.initialize`) is the Swift / headless
entry. `initializeWidget` is the CMP widget entry from `:feature:characters:ui`.

## Known gaps

- The XCFramework and `compileKotlinIosSimulatorArm64` only run on macOS.
- GitLab Free has no macOS SaaS runners; the `ios` CI job uses `allow_failure: true` until a Mac
  runner is available. See [ci-components.md](ci-components.md).
- ABI validation on Linux only keeps previously recorded Apple slices. See
  [api-compatibility.md](api-compatibility.md).
- The Swift wrapper’s Kotlin `Result` mapping assumes the usual K/N names (`ResultSuccess` /
  `ResultError`). Verify on macOS after assembling the XCFramework if interop names change.
