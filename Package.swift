// swift-tools-version:5.9
import PackageDescription

// Local-development form: the binary target points at the XCFramework produced by
//   ./gradlew :runtime:assembleRickAndMortySDKCoreReleaseXCFramework
// so an iOS engineer can consume the SDK exactly the way a partner will, without waiting for a
// release. See docs/ios-integration.md.
//
// Host apps should depend on the **RickAndMortySDK** product (Swift wrapper). That product
// re-exports the Kotlin XCFramework and owns the KMP-NativeCoroutines dependency so consumers
// do not add NativeCoroutines themselves.
//
// The release pipeline rewrites the binary target below into its remote form:
//
//   .binaryTarget(
//       name: "RickAndMortySDKCore",
//       url: "https://gitlab.com/<group>/rick_and_morty/-/releases/<tag>/downloads/RickAndMortySDKCore.xcframework.zip",
//       checksum: "<swift package compute-checksum output>"
//   )
//
// The checksum is why this cannot be hand-written ahead of a release: it is derived from the zip,
// so the manifest is only truthful once the artifact exists.
let package = Package(
    name: "RickAndMortySDK",
    platforms: [
        .iOS(.v14),
        // SPM resolves macOS platform requirements on the Mac host even for iOS-only builds.
        .macOS(.v10_15),
    ],
    products: [
        .library(
            name: "RickAndMortySDK",
            targets: ["RickAndMortySDK"]
        )
    ],
    dependencies: [
        .package(
            url: "https://github.com/rickclephas/KMP-NativeCoroutines.git",
            exact: "1.0.4"
        )
    ],
    targets: [
        .binaryTarget(
            name: "RickAndMortySDKCore",
            path: "runtime/build/XCFrameworks/release/RickAndMortySDKCore.xcframework"
        ),
        .target(
            name: "RickAndMortySDK",
            dependencies: [
                "RickAndMortySDKCore",
                .product(name: "KMPNativeCoroutinesAsync", package: "KMP-NativeCoroutines")
            ],
            path: "swift/Sources/RickAndMortySDK"
        )
    ]
)
