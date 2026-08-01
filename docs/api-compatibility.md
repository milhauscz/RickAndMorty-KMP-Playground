# API compatibility policy

This document covers every module that applies `rickandmorty.kmp.published`: `:runtime`,
`:core:common`, `:core:featureflags`, the three feature `api` modules, the three feature `impl`
modules, and `:feature:characters:ui`. Everything else (`:shared`, `:androidApp`, internal `core`
modules) is not under the compatibility policy.

## What "public API" means here

A declaration is part of a module's contract if it appears in that module's committed
`api/*.klib.api` dump. The file is generated from compiled artifacts, so it reflects what consumers
can link against.

Two mechanisms keep the dumps honest:

- `explicitApi()` rejects declarations that do not state their visibility.
- `checkKotlinAbi` compares a fresh dump against the committed one and fails on any difference.

Types annotated `@InternalRickAndMortyApi` or `@InternalRickAndMortyRuntimeApi` are excluded from the
dump.

## Versioning

The SDK follows semantic versioning against the ABI dumps.

| Change | Version bump |
| --- | --- |
| Adding a new type, function, or property | Minor |
| Adding a member to an interface consumers implement | Major |
| Adding an enum constant | Major |
| Adding a parameter, even with a default value | Major |
| Removing or renaming anything public | Major |
| Changing a parameter or return type | Major |
| Widening a return type, narrowing a parameter type | Major |
| Behaviour change with an unchanged signature | Minor, and a changelog entry |
| Bug fix with no signature change | Patch |

## Deprecation ladder

Nothing public is removed without going through all three steps:

1. `@Deprecated(level = WARNING)` with `ReplaceWith` where possible — minor release.
2. `@Deprecated(level = ERROR)` — later minor release.
3. Removal — major release only.

Experimental API annotated `@ExperimentalRickAndMortyApi` can change in any release.

## Updating the dumps

When a change to a published module's API is intentional:

```bash
./gradlew updateKotlinAbi
```

Commit every regenerated `api/*.klib.api` in the same commit as the code change.

## Known coverage gaps

- **Apple targets need a macOS host.** `keepLocallyUnsupportedTargets` is on; iOS-only API changes
  may reach CI unnoticed until a macOS runner exists.
- **The Android artifact's JVM ABI is not dumped.** Nearly the whole surface lives in `commonMain`.
- **Behaviour is not an ABI.** That is what tests and the changelog are for.
