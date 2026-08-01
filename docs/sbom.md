# Software bill of materials

A partner integrating an SDK inherits its dependency graph. The CycloneDX plugin answers "what is
inside the thing we shipped" from a file.

## Generating one

```bash
./gradlew :runtime:cyclonedxDirectBom   # runtime/build/reports/cyclonedx-direct/bom.json
./gradlew cyclonedxBom                  # build/reports/sbom/sbom.json — whole build
```

The per-artifact document describes what a consumer of `cz.cernilovsky.kmp.rickandmorty:runtime`
resolves. The aggregate covers the whole build including `:androidApp` and `:konsist`.

## Uploading it

See `templates/sbom/template.yml`. With `upload-url` empty the document is generated and kept as an
artifact only.

## Headless vs widget

After splitting each feature's `impl` into data (`impl`) and screens (`ui`), the headless `:runtime`
artifact no longer pulls in Compose through character screens. Widget consumers add
`:feature:characters:ui`, which intentionally brings Compose.

## Not done here

- No vulnerability scanning, SBOM diffing between releases, or attestation/signing.
