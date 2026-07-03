import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** Access the shared `libs` version catalog from precompiled script plugins. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).get().requiredVersion

/** compileSdk from the catalog. */
internal val Project.androidCompileSdk: Int
    get() = libs.version("android-compileSdk").toInt()

/** minSdk from the catalog. */
internal val Project.androidMinSdk: Int
    get() = libs.version("android-minSdk").toInt()

/**
 * Derive a unique Android namespace from the module path, e.g.
 * `:core:database` -> `cz.cernilovsky.kmp.rickandmorty.core.database`.
 */
internal val Project.androidNamespace: String
    get() = "cz.cernilovsky.kmp.rickandmorty." +
        path.removePrefix(":").replace(":", ".").replace("-", "")
