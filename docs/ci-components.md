# CI components

`.gitlab-ci.yml` used to be one file describing one project's pipeline. The reusable half now lives
in `templates/` as [GitLab CI components](https://docs.gitlab.com/ci/components/), and the root
pipeline consumes them from this same project — which is the part that matters, because a component
its own pipeline does not use is a component nobody has verified.

## The components

| Component | Job | What it does |
| --- | --- | --- |
| `base` | — | `workflow`, `stages`, `.mrJobRules`, `.releaseJobRules`, and Gradle/Android `default`. Include once. |
| `gradle-quality` | `lint`, `changelog` | kotlinter, detekt, Konsist, `checkKotlinAbi`, and a changelog-section check for `VERSION_NAME`. |
| `gradle-test` | `test` | Host tests across every module, with JUnit results attached to the pipeline. |
| `android-build` | `build` | Assembles an APK for a chosen build type and keeps it as an artifact. |
| `ios-build` | `ios` | Compiles `iosSimulatorArm64`, runs `:runtime:iosSimulatorArm64Test`, and assembles the XCFramework on a **macOS** runner (`allow_failure: true` on GitLab Free, which has no macOS SaaS runners). |
| `release` | `deployLibs`, `deliverAndroidApp`, `publish-release` | Publishes library artifacts, assembles the release APK, and creates a GitLab release. |
| `sbom` | `sbom` | Generates a CycloneDX SBOM for the published artifact and, if a receiver is configured, uploads it. See [sbom.md](sbom.md). |

Each job component takes `stage` and task-specific inputs, so the same component can be a
merge-request gate in one project and a nightly job in another. `job-name` exists so a component can
be included twice — a debug build on merge requests and a release build on the default branch are the
same component with different inputs. Job rules come from `base`: `.mrJobRules` for lint/test/build
jobs, `.releaseJobRules` for release-stage jobs (automatic on `development`, manual on web elsewhere).
Image, `JAVA_HOME` and Gradle cache come from `base` as well.

## Consuming them

```yaml
include:
  - component: $CI_SERVER_FQDN/cernilovsky/rick_and_morty/base@1.0.0
  - component: $CI_SERVER_FQDN/cernilovsky/rick_and_morty/gradle-quality@1.0.0
    inputs:
      stage: verify
      gradle-args: lintKotlin detekt
```

Inputs are validated by GitLab before the pipeline is created, so a typo in an input name or a value
outside `options` fails immediately with a message naming it, rather than producing a job that runs
and does the wrong thing.

## Versioning them

The root pipeline pins `@$CI_COMMIT_SHA`, so a change to a template is exercised by the pipeline of
the merge request that changes it. Anyone else pins a release tag: components are versioned with the
project, and a tag that moves under consumers is the whole problem components exist to avoid.

The same reasoning as the SDK applies to a template's inputs. Removing an input, or adding a required
one without a default, breaks every consumer at pipeline creation — so inputs get defaults, and are
deprecated in a description before they are removed.

## Publishing to the CI/CD Catalog

Listing in the catalog needs four things, three of which the repository already has:

1. Components as `templates/<name>.yml`, each with a `spec:` header. ✔
2. A root `README.md` describing the project. ✔
3. Releases created with the `release` keyword — the `publish-release` job does this. ✔
4. The project marked as a catalog project in **Settings → General → Visibility → CI/CD Catalog
   project**, which is a switch in the UI rather than anything in this repository.

Until step 4 is done the components are still usable by path; the catalog only affects discovery.
