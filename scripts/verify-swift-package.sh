#!/usr/bin/env bash
set -euo pipefail

# Smoke-check the RickAndMortySDK Swift package for the iOS Simulator.
# Requires the release XCFramework from Gradle (see docs/ios-integration.md).

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

XCFRAMEWORK="runtime/build/XCFrameworks/release/RickAndMortySDKCore.xcframework"
if [[ ! -d "$XCFRAMEWORK" ]]; then
  echo "Missing $XCFRAMEWORK — run ./gradlew :runtime:assembleRickAndMortySDKCoreReleaseXCFramework first" >&2
  exit 1
fi

# Fail fast if KSP did not emit refined NativeCoroutines wrappers.
# Refined APIs are marked swift_private in the ObjC header (Swift sees them as __loadCharacters).
HEADER="$(find "$XCFRAMEWORK" -name 'RickAndMortySDKCore.h' | head -n 1)"
if [[ -z "$HEADER" ]]; then
  echo "Missing RickAndMortySDKCore.h inside $XCFRAMEWORK" >&2
  exit 1
fi
if ! grep -q 'loadCharacters' "$HEADER"; then
  echo "CharactersIosBridge.loadCharacters not found in framework header — KSP may not have run." >&2
  echo "Clean and rebuild: ./gradlew :runtime:clean :runtime:assembleRickAndMortySDKCoreReleaseXCFramework" >&2
  exit 1
fi
if ! grep -q 'swift_private' "$HEADER"; then
  echo "No swift_private symbols in framework header — @NativeCoroutinesRefined wrappers may be missing." >&2
  echo "Ensure kmp-nativecoroutines annotations are on commonMain and rebuild the XCFramework." >&2
  exit 1
fi

SDKROOT="$(xcrun --sdk iphonesimulator --show-sdk-path)"
swift build \
  --triple arm64-apple-ios14.0-simulator \
  --sdk "$SDKROOT"
