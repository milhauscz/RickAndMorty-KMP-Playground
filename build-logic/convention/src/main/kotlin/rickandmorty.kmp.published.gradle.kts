import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

/**
 * For modules whose API is a product: `explicitApi()` and ABI validation.
 *
 * Apply alongside `rickandmorty.kmp.library` or `rickandmorty.kmp.feature`.
 */
plugins {
    id("maven-publish")
}

val displayName = path.removePrefix(":").replace(':', '-')

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Rick and Morty SDK ($displayName)")
            description.set(
                "Kotlin Multiplatform SDK for browsing the Rick and Morty API. Public, " +
                    "ABI-validated surface; see docs/api-compatibility.md for the compatibility policy.",
            )
        }
    }
}

configure<KotlinMultiplatformExtension> {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        keepLocallyUnsupportedTargets.set(true)
        filters {
            exclude {
                annotatedWith.add("cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi")
            }
        }
    }
}
