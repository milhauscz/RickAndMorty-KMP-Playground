package cz.cernilovsky.kmp.rickandmorty.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.provider.KoAnnotationProvider
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class PublicApiDisciplineTest {
    private val charactersUiPackage = "..characters.ui.."
    @Test
    fun `view models are internal api`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.hasInternalRickAndMortyApiAnnotation() }
    }

    @Test
    fun `public koin module vals are internal api`() {
        Konsist
            .scopeFromProduction()
            .properties()
            .withNameEndingWith("Module")
            .assertTrue { !it.hasPublicModifier || it.hasInternalRickAndMortyApiAnnotation() }
    }

    @Test
    fun `public ui package classes are internal api`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .assertTrue {
                !it.resideInPackage(charactersUiPackage) ||
                    !it.hasPublicModifier ||
                    it.hasInternalRickAndMortyApiAnnotation()
            }
    }

    @Test
    fun `public ui package functions are allowlisted screens or internal api`() {
        val allowedPublicUiFunctions =
            setOf(
                "CharacterListDetailScreen",
                "CharacterFiltersScreen",
            )

        Konsist
            .scopeFromProduction()
            .functions()
            .assertTrue {
                !it.resideInPackage(charactersUiPackage) ||
                    !it.hasPublicModifier ||
                    !it.isTopLevel ||
                    it.name in allowedPublicUiFunctions ||
                    it.hasInternalRickAndMortyApiAnnotation()
            }
    }

    private fun KoAnnotationProvider.hasInternalRickAndMortyApiAnnotation(): Boolean =
        hasAnnotationWithName("InternalRickAndMortyApi")
}
