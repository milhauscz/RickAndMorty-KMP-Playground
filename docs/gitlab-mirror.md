# Mirroring to GitLab

The pipeline in `.gitlab-ci.yml` only means something once it has run. GitHub stays the place the
repository is developed; GitLab is where CI, the Package Registry and the CI/CD Catalog live, because
those are the features being demonstrated.

## Why push, not pull

GitLab's *pull* mirroring — GitLab periodically fetching from GitHub — is a paid feature. *Push*
mirroring (GitLab → GitHub) is free, as is pushing to both remotes from a working copy. Since commits
originate locally, the second option costs nothing and has no delay:

```bash
git remote set-url --add --push origin https://github.com/milhauscz/RickAndMorty-KMP-Playground.git
git remote set-url --add --push origin https://gitlab.com/<namespace>/rick_and_morty.git
```

The first command is needed because adding a push URL replaces the implicit one. After this,
`git push origin <branch>` writes to both, and `git remote -v` lists two push URLs.

If you would rather keep the remotes separate:

```bash
git remote add gitlab https://gitlab.com/<namespace>/rick_and_morty.git
git push gitlab <branch>
```

## Setting it up

1. Create an empty project at `gitlab.com/<namespace>/rick_and_morty` — no README, no `.gitignore`;
   the push provides both.
2. Point `PROJECT_URL` in `gradle.properties` at it. It is the single source for the POM `url` and
   `scm` blocks, so a stale value ships inside every published artifact.
3. Add the push URL as above and push `development`.
4. In **Settings → CI/CD → Runners**, confirm instance runners are enabled. The free tier includes
   400 compute minutes a month, which is the real constraint on this pipeline — the Android build
   dominates it.
5. Check that the image in `.gitlab-ci.yml` (`cernilovsky/android-kmp:jdk17`) is public on Docker Hub
   and has the Android SDK and JDK 17 the build expects. A missing or private image fails every job
   at the pull step, before any script line runs.

## Verified before the first run

Everything a runner would execute has been run on a developer machine, so the first pipeline is
exercising GitLab rather than debugging Gradle:

| Job | Command | Result |
| --- | --- | --- |
| `lint` | `./gradlew lintKotlin detekt :konsist:test checkKotlinAbi` | Passes; ABI dumps match. |
| `changelog` | `./scripts/changelog-section.sh` | Extracts 0.1.0, and exits non-zero for a version with no section. |
| `test` | `./gradlew testAndroidHostTest` | Passes across every module. |
| `build` | `./gradlew :androidApp:assembleDebug` | Produces the APK. |
| `publish-maven` | `./gradlew publishAllPublicationsToLocalTestRepository` | Every library module publishes; `:shared` is the integration demo. |
| `sbom` | `./gradlew :runtime:cyclonedxDirectBom` | Produces the document; the upload half runs against `scripts/mock-sbom-receiver.mjs`. |

Every `template.yml` and the root `.gitlab-ci.yml` parse as YAML. What that does *not* cover is
GitLab's own validation of `spec.inputs` and `include: component:`, which only happens server-side —
run the configuration through **CI/CD → Editor → Validate** before the first push, or accept that the
first pipeline is where a typo in an input name shows up.

## Verifying the first run

`workflow.rules` restricts pipelines to merge requests, pushes to `development`, and manual runs from
the UI. A push to a feature branch deliberately produces nothing, so trigger the first run from
**Build → Pipelines → Run pipeline**, which matches `$CI_PIPELINE_SOURCE == "web"`.

What each job proves, in order:

| Job | Passing means |
| --- | --- |
| `lint` | kotlinter, detekt and Konsist run on the runner, the committed ABI dumps match, and the version being released has a changelog section. |
| `test` | Host tests across every module, with results attached to the pipeline as JUnit reports. |
| `build` | `:androidApp:assembleDebug` completes with the SDK installed on the runner. |
| `publish-maven` | `CI_JOB_TOKEN` authenticates against the Package Registry and the coordinates land under **Deploy → Package Registry**. Only on `development`. |
| `release` | The release APK builds and `release-notes.md` is extracted from the changelog. |
| `publish-release` | A tag and a GitLab release appear under **Deploy → Releases**, with the changelog as the description and links to the APK and the packages. |

Two things that only show up on the first run:

- **Gradle cache.** The `.gradle/` cache is keyed per branch, so the first pipeline on a branch
  downloads everything. Judge job durations from the second run.
- **Repeated tags.** `publish-release` derives its tag from `VERSION_NAME`. Pushing to `development`
  twice without bumping the version fails the second release job, because the tag already exists.
  That is the intended behaviour — it is the check that a version is released once — and it is why
  bumping the version and writing the changelog entry belong in the same merge request.

## Known gap

Apple targets need a macOS runner, which the free tier does not include. `checkKotlinAbi` therefore
infers the Apple contribution on the Linux runner rather than compiling it; see the coverage gaps in
[api-compatibility.md](api-compatibility.md).
