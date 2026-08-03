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

`SdkMode.Headless` (the default) is the Swift-friendly path. Initialize once at app start, then call
into exported domain types from a small Kotlin bridge when you need Flows / coroutines — or render
exported models in SwiftUI after a Kotlin helper fetches them.

```swift
import RickAndMortySDK

RickAndMortySdkIosKt.initialize(
    config: RickAndMortySdkConfig.companion.builder()
        .mode(mode: .headless)
        .baseUrl(value: "https://rickandmortyapi.com/api")
        .build()
)
```

The XCFramework exports `:runtime`, `:feature:characters:api`, `:feature:characters:impl`, and
`:core:common`. Domain models (`Character`, filters, `Result`) are visible to Swift. Paging / `Flow`
APIs are awkward to consume directly from Swift; prefer a thin Kotlin façade that exposes `suspend`
or callback-based helpers if the host UI is SwiftUI/UIKit.

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
