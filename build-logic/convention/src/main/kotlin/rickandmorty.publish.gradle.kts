import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.authentication.http.HttpHeaderAuthentication

/**
 * Maven coordinates, POM metadata and repository wiring for every module that can end up in a
 * published artifact's dependency graph.
 *
 * Applied to all library modules rather than only to `:runtime`, because a project dependency is
 * recorded in the published metadata as real coordinates. Publishing only the facade would ship a
 * POM pointing at `unspecified` versions of modules that exist nowhere — resolvable on this machine
 * and nowhere else. Being published is therefore not the same as being public: what a consumer may
 * rely on is decided by `rickandmorty.kmp.published`. See docs/api-compatibility.md.
 */
plugins {
    id("maven-publish")
}

// The group mirrors the module path, exactly as the Android namespace does, so `:core:common`
// becomes `cz.cernilovsky.kmp.rickandmorty.core:common`. This is not cosmetic: inside a build a
// project's identity is group + name, and `:feature:characters:api` and `:feature:episode:api` are
// both named `api`. Give them one group and Gradle resolves them as a single module, which shows up
// as a circular task dependency rather than as anything resembling the actual mistake.
group = providers.gradleProperty("GROUP").map { base ->
    val parentPath = path.substringBeforeLast(':').removePrefix(":").replace(':', '.')
    if (parentPath.isEmpty()) base else "$base.$parentPath"
}.get()
version = providers.gradleProperty("VERSION_NAME").get()

val projectUrl = providers.gradleProperty("PROJECT_URL").get()
val displayName = path.removePrefix(":").replace(':', '-')

// The Kotlin Multiplatform plugin registers the publications (one per target plus the metadata
// publication) and attaches a sources jar to each, so there is nothing to create here — only
// metadata to fill in. No javadoc jar: without Dokka wired up it would be an empty archive that
// exists purely to satisfy a validator, and the GitLab registry does not ask for one.
publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set(displayName)
            description.set(
                "Implementation module of the Rick and Morty SDK. Published so that the SDK's own " +
                    "coordinates resolve; not part of the supported API surface.",
            )
            url.set(projectUrl)
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("cernilovsky")
                    name.set("Milos Cernilovsky")
                }
            }
            scm {
                url.set(projectUrl)
                connection.set("scm:git:$projectUrl.git")
            }
        }
    }

    repositories {
        // Always present, so `publishAllPublicationsToLocalTestRepository` can verify the artifacts
        // and their POMs are well-formed without a network or a token.
        maven {
            name = "localTest"
            url = uri(rootProject.layout.buildDirectory.dir("local-maven"))
        }

        // GitLab's Package Registry, addressed entirely through variables the runner injects, so no
        // credential ever lives in the repository and nothing has to be configured for a fork.
        // Absent outside CI, which keeps `publish` from failing on a developer machine.
        val apiUrl = providers.environmentVariable("CI_API_V4_URL").orNull
        val projectId = providers.environmentVariable("CI_PROJECT_ID").orNull
        if (apiUrl != null && projectId != null) {
            maven {
                name = "GitLab"
                url = uri("$apiUrl/projects/$projectId/packages/maven")
                credentials(HttpHeaderCredentials::class) {
                    name = "Job-Token"
                    value = providers.environmentVariable("CI_JOB_TOKEN").orNull
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }
}
