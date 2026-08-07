#!/usr/bin/env bash
set -euo pipefail

# Smoke-check the RickAndMortySDK Swift package for the iOS Simulator.
# Requires the release XCFramework from Gradle (see docs/ios-integration.md).

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

XCFRAMEWORK="runtime/build/XCFrameworks/release/RickAndMortySDK.xcframework"
if [[ ! -d "$XCFRAMEWORK" ]]; then
  echo "Missing $XCFRAMEWORK — run ./gradlew :runtime:assembleRickAndMortySDKReleaseXCFramework first" >&2
  exit 1
fi

SDKROOT="$(xcrun --sdk iphonesimulator --show-sdk-path)"
swift build \
  --triple arm64-apple-ios14.0-simulator \
  --sdk "$SDKROOT"
