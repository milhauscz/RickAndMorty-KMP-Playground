package cz.cernilovsky.kmp.rickandmorty.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPath
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

private const val ROOT_PACKAGE = "cz.cernilovsky.kmp.rickandmorty"
private val FEATURES = listOf("characters", "episode", "location")

class FeatureModuleArchitectureTest {
    @Test
    fun `feature api production sources reside in domain packages`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPath("/api/src/commonMain/")
            .withPath("/feature/")
            .assertTrue { it.hasPackage("..domain..") }
    }

    @Test
    fun `feature api production sources do not import data ui or di packages`() {
        val forbiddenLayerPackages = listOf(".data.", ".ui.", ".di.")
        Konsist
            .scopeFromProduction()
            .files
            .withPath("/api/src/commonMain/")
            .withPath("/feature/")
            .assertFalse { file ->
                file.imports.any { import ->
                    forbiddenLayerPackages.any { layer -> import.name.contains(layer) }
                }
            }
    }

    @Test
    fun `feature impl production sources do not import foreign feature data or di packages`() {
        FEATURES.forEach { feature ->
            val foreignFeatures = FEATURES - feature
            Konsist
                .scopeFromProduction()
                .files
                .withPath("/feature/$feature/impl/")
                .assertFalse { file ->
                    file.imports.any { import ->
                        foreignFeatures.any { other ->
                            import.name.startsWith("$ROOT_PACKAGE.$other.data.") ||
                                import.name.startsWith("$ROOT_PACKAGE.$other.di.")
                        }
                    }
                }
        }
    }
}
