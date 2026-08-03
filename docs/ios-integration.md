# iOS integration

## Consuming the SDK

The SDK ships as `RickAndMortySDK.xcframework` (`iosArm64` + `iosSimulatorArm64`). Build it with:

```bash
./gradlew :runtime:assembleRickAndMortySDKReleaseXCFramework
```

The output lands in `runtime/build/XCFrameworks/release/`, where the root `Package.swift` points.

```swift
import RickAndMortySDK

RickAndMortySdkIosKt.initialize(
    config: RickAndMortySdkConfig.companion.builder()
        .baseUrl(value: "https://rickandmortyapi.com/api")
        .build()
)
```

## Swift surface

The XCFramework exports `:runtime`, `:feature:characters:api`, `:feature:characters:impl`, and
`:core:common`. Domain models and `Result` types cross the bridge directly.

**iOS UI is Compose Multiplatform.** Character screens live in `:feature:characters:ui` and are
consumed from Kotlin/Compose, not from Swift. Swift integrators use the headless surface (repositories,
use cases, models). If native UI is required, call into the exported domain layer and render with
UIKit/SwiftUI.

**Platform-specific `initialize`.** Android needs a `Context`; iOS does not. On iOS the entry point
is `RickAndMortySdkIosKt.initialize(config:)`.

## Known gaps

- The XCFramework can only be assembled on macOS.
- ABI validation infers Apple slices on non-macOS hosts. See `docs/api-compatibility.md`.
