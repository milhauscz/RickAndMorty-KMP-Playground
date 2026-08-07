# Publishing and coordinates

How artifacts get from this repository into a consumer's dependency block.

## One version, everywhere

`gradle.properties` holds the only version:

```properties
GROUP=cz.cernilovsky.kmp.rickandmorty
VERSION_NAME=0.3.0
PROJECT_URL=https://gitlab.com/milhauscz-mobile/RickAndMorty-KMP-Playground/
```

Which way to bump it is decided by [api-compatibility.md](api-compatibility.md).

## Coordinates

The group mirrors the module path:

| Module | Coordinates |
| --- | --- |
| `:runtime` | `cz.cernilovsky.kmp.rickandmorty:runtime` |
| `:feature:characters:ui` | `cz.cernilovsky.kmp.rickandmorty.feature.characters:ui` |
| `:feature:characters:impl` | `cz.cernilovsky.kmp.rickandmorty.feature.characters:impl` |
| `:feature:characters:api` | `cz.cernilovsky.kmp.rickandmorty.feature.characters:api` |
| `:core:common` | `cz.cernilovsky.kmp.rickandmorty.core:common` |

Kotlin Multiplatform adds target suffixes through Gradle module metadata.

## Published is not the same as public

Every library module is published so dependency graphs resolve outside this repository. What a
consumer may rely on is decided by `rickandmorty.kmp.published`:

- **Published + ABI-validated:** `:runtime`, `:core:common`, `:core:featureflags`, feature `api`/`impl`
  modules, and `:feature:characters:ui`.
- **Published, no guarantee:** other `core` modules (`:core:network`, `:core:database`, etc.).

`:shared` is not published — it is the demo app.

## Consuming it

**Headless** (data only):

```kotlin
dependencies {
    implementation("cz.cernilovsky.kmp.rickandmorty:runtime:0.3.0")
    implementation("cz.cernilovsky.kmp.rickandmorty.feature.characters:impl:0.3.0")
}
```

**Widget** (screens + data):

```kotlin
dependencies {
    implementation("cz.cernilovsky.kmp.rickandmorty:runtime:0.3.0")
    implementation("cz.cernilovsky.kmp.rickandmorty.feature.characters:ui:0.3.0")
}
```

Call `RickAndMortySdk.initializeWidget(...)` from `:feature:characters:ui` so the character UI
Koin module is registered automatically (`SdkMode.Widget`).

**Headless** resolution after `initialize`:

```kotlin
val useCase = RickAndMortySdk.get<GetCharactersUseCase>()
```

Supported public types are listed in [api-compatibility.md](api-compatibility.md).

iOS consumers use the XCFramework — see [ios-integration.md](ios-integration.md).

### Local verification

```bash
./gradlew publishAllPublicationsToLocalTestRepository
```

### Requirements

| Requirement | Value |
| --- | --- |
| Kotlin | 2.4.0 or newer |
| Android | minSdk 24, compileSdk 37 |
| JVM target | 11 or newer |
