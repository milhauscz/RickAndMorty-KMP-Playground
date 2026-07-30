# Porting Konsist architecture checks to native Android

Instructions for an agent setting up Konsist in the **native Android** Rick and Morty project, mirroring what was done in this KMP repo (`feature/api-impl-modularization` branch, commit `3d9068d`).

## Goal

Add a dedicated JVM `:konsist` module that scans **all production Kotlin sources** in the project and enforces:

1. **Layer rules** — `domain` is independent; `data` / `ui` / `di` depend only on allowed layers
2. **Api/impl boundaries** — api modules contain domain-only code; impl modules don't import foreign feature `data` or `di`
3. **Naming conventions** — `Repository`, `RepositoryImpl`, `UseCase`, `Dto` live in the right packages

Konsist checks **package/import rules**, not Gradle `build.gradle.kts` dependency edges. That matches how this codebase enforces architecture after the api/impl split.

## Why a separate JVM module

[Konsist recommends isolating tests](https://docs.konsist.lemonappdev.com/advanced/isolate-konsist-tests.md) in a dedicated JVM module because it scans the **entire project** from one place — not a single Android variant or source set.

Do **not** put Konsist tests inside `:app` or a feature module's `androidTest`/`test` folder.

## Step 1 — Version catalog

In `gradle/libs.versions.toml` add:

```toml
[versions]
junit-jupiter = "5.11.4"
konsist = "0.17.3"

[libraries]
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }
konsist = { module = "com.lemonappdev:konsist", version.ref = "konsist" }

[plugins]
kotlinJvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
```

(`kotlin` version should already exist in the catalog.)

## Step 2 — Include the module

In `settings.gradle.kts`:

```kotlin
include(":konsist")
```

## Step 3 — Module build script

Create `konsist/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
    // Konsist scans all modules; avoid stale skips when only other modules changed.
    outputs.upToDateWhen { false }
}
```

**Do not** add `jvmToolchain(17)` unless the project already auto-provisions JDK toolchains. This KMP repo omitted it because local Windows builds failed without a Foojay resolver; CI already runs on JDK 17.

## Step 4 — Root convenience task

In the **root** `build.gradle.kts`, register the JVM plugin and an alias task:

```kotlin
plugins {
    // ...existing plugins...
    alias(libs.plugins.kotlinJvm) apply false
}

tasks.register("konsistCheck") {
    group = "verification"
    description = "Runs Konsist architecture checks"
    dependsOn(":konsist:test")
}
```

## Step 5 — CI

Add `:konsist:test` to the existing lint job (alongside kotlinter/detekt if present):

```yaml
lint:
  script:
    - ./gradlew lintKotlin detekt :konsist:test
```

In this KMP repo the lint job is MR-only (`.gitlab-ci.yml`).

## Step 6 — Test package and constants

Create tests under:

```
konsist/src/test/kotlin/<base-package>/konsist/
```

Use the **native Android base package** (likely `cz.cernilovsky.rickandmorty`, without `.kmp`). Define:

```kotlin
private const val ROOT_PACKAGE = "cz.cernilovsky.rickandmorty" // adjust to native project
private val FEATURES = listOf("characters", "episode", "location")
```

## Step 7 — Port the three test files

Copy the logic from this repo's `konsist/src/test/kotlin/cz/cernilovsky/kmp/rickandmorty/konsist/` and adapt **paths + package wildcards** only. The rules themselves stay the same.

### 7a. `LayerArchitectureTest.kt`

```kotlin
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import org.junit.jupiter.api.Test

class LayerArchitectureTest {
    @Test
    fun `clean architecture layers have correct dependencies`() {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                val domain = Layer("Domain", "$ROOT_PACKAGE..domain..")
                val data = Layer("Data", "$ROOT_PACKAGE..data..")
                val ui = Layer("UI", "$ROOT_PACKAGE..ui..")
                val di = Layer("DI", "$ROOT_PACKAGE..di..")

                domain.dependsOnNothing()
                data.dependsOn(domain)
                ui.dependsOn(domain)
                di.dependsOn(domain)
                di.dependsOn(data)
            }
    }
}
```

Replace `$ROOT_PACKAGE` with the literal base package string (e.g. `"cz.cernilovsky.rickandmorty"`).

**Known exception (both projects):** `CharactersRepository` intentionally references `androidx.paging.PagingData` in the api/domain layer. Do **not** add a strict "no Android in domain" rule.

### 7b. `FeatureModuleArchitectureTest.kt`

**Path filters must match native module layout.** This KMP repo uses:

| Rule | KMP path filter |
| --- | --- |
| Api sources in domain packages | `.withPath("/api/src/commonMain/").withPath("/feature/")` |
| Api must not import data/ui/di | same |
| Impl must not import foreign data/di | `.withPath("/feature/$feature/impl/")` |

For **native Android** with the same api/impl module split, change to `main` source sets:

| Rule | Native Android path filter (expected) |
| --- | --- |
| Api sources in domain packages | `.withPath("/api/src/main/").withPath("/feature/")` |
| Api must not import data/ui/di | same |
| Impl must not import foreign data/di | `.withPath("/feature/$feature/impl/src/main/")` |

Verify actual directory names in the native repo before committing — adjust `withPath` strings to match real paths (e.g. `feature-characters-api` vs `feature/characters/api`).

**File-level package check:** use `it.hasPackage("..domain..")`, not `it.resideInPackage(...)` — `resideInPackage` does not compile on `KoFileDeclaration`.

```kotlin
import com.lemonappdev.konsist.api.ext.list.withPath
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
```

### 7c. `NamingConventionTest.kt`

No path changes needed — naming rules are package-based:

```kotlin
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
```

Rules:

- `*Repository` (not `*RepositoryImpl`) → `..domain..`
- `*RepositoryImpl` → `..data..`
- `*UseCase` → `..domain..`
- `*Dto` → `..data..`
- Nothing in `..domain..` ends with `Dto` or `Entity`

## KMP → native Android cheat sheet

| Concern | This KMP repo | Native Android (expected) |
| --- | --- | --- |
| Base package | `cz.cernilovsky.kmp.rickandmorty` | `cz.cernilovsky.rickandmorty` (confirm in native repo) |
| Production sources | `commonMain`, `androidMain`, etc. | `src/main/` per module |
| Api module path | `feature/<name>/api/src/commonMain/` | `feature/<name>/api/src/main/` |
| Impl module path | `feature/<name>/impl/` | `feature/<name>/impl/src/main/` |
| Scope | `Konsist.scopeFromProduction()` | Same — excludes `test`, `androidTest`, `commonTest` |
| Test runner | JUnit 5 (`useJUnitPlatform`) | Same |

## Import cheat sheet (common compile errors)

| API | Required import |
| --- | --- |
| Entry point | `com.lemonappdev.konsist.api.Konsist` |
| Architecture assertion | `com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture` |
| Layer definition | `com.lemonappdev.konsist.api.architecture.Layer` |
| Path / name filters | `com.lemonappdev.konsist.api.ext.list.withPath`, `.withNameEndingWith`, `.withPackage` |
| Assertions | `com.lemonappdev.konsist.api.verify.assertTrue`, `.assertFalse` |

`assertArchitecture` is **not** resolved by importing `Konsist` alone — that was the main compile failure during setup.

Inside `assertArchitecture { }`, `dependsOn` / `dependsOnNothing` need no extra imports (receiver is `LayerDependencies`).

| Declaration type | Package check |
| --- | --- |
| Classes / interfaces | `it.resideInPackage("..domain..")` |
| Files | `it.hasPackage("..domain..")` |
| Filter list | `.withPackage("..domain..")` |

## Step 8 — Verify

```bash
./gradlew :konsist:test -x lintKotlin -x detekt
# or
./gradlew konsistCheck
```

All tests should pass **without production code changes** if the native project already follows the same api/impl + layer structure.

If a test fails:

1. Print failing declarations with Konsist's `print()` while debugging
2. Check whether `withPath` filters match the native directory layout
3. Check whether the base package string is correct

## Step 9 — Document in agent instructions

Add to the native project's `CLAUDE.md` (or equivalent):

```bash
./gradlew :konsist:test    # architecture / layer checks
./gradlew konsistCheck     # alias
```

## Reference files in this repo

| File | Purpose |
| --- | --- |
| `konsist/build.gradle.kts` | Module setup |
| `konsist/src/test/kotlin/.../LayerArchitectureTest.kt` | Layer dependency rules |
| `konsist/src/test/kotlin/.../FeatureModuleArchitectureTest.kt` | Api/impl + cross-feature import rules |
| `konsist/src/test/kotlin/.../NamingConventionTest.kt` | Naming conventions |
| `gradle/libs.versions.toml` | `konsist`, `junit-jupiter`, `kotlinJvm` entries |
| `settings.gradle.kts` | `include(":konsist")` |
| `build.gradle.kts` | `konsistCheck` task |
| `.gitlab-ci.yml` | `:konsist:test` in lint job |

## Out of scope (optional follow-up)

Gradle module dependency graph assertions (e.g. `characters:impl` must not depend on `episode:impl`) are **not** covered by Konsist. Consider [modules-graph-assert](https://github.com/jraska/modules-graph-assert) later if build-script-level enforcement is needed in addition to import rules.
