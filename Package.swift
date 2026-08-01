// swift-tools-version:5.9
import PackageDescription

// Local-development form: points at the XCFramework produced by
//   ./gradlew :runtime:assembleRickAndMortySDKReleaseXCFramework
// so an iOS engineer can consume the SDK exactly the way a partner will, without waiting for a
// release. See docs/ios-integration.md.
//
// The release pipeline rewrites the target below into its remote form, which is what a partner
// actually resolves:
//
//   .binaryTarget(
//       name: "RickAndMortySDK",
//       url: "https://gitlab.com/<group>/rick_and_morty/-/releases/<tag>/downloads/RickAndMortySDK.xcframework.zip",
//       checksum: "<swift package compute-checksum output>"
//   )
//
// The checksum is why this cannot be hand-written ahead of a release: it is derived from the zip,
// so the manifest is only truthful once the artifact exists.
let package = Package(
    name: "RickAndMortySDK",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: "RickAndMortySDK",
            targets: ["RickAndMortySDK"]
        )
    ],
    targets: [
        .binaryTarget(
            name: "RickAndMortySDK",
            path: "runtime/build/XCFrameworks/release/RickAndMortySDK.xcframework"
        )
    ]
)
