package cz.cernilovsky.kmp.rickandmorty.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class NamingConventionTest {
    @Test
    fun `repository interfaces reside in domain package`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith("Repository")
            .assertTrue { !it.name.endsWith("RepositoryImpl") && it.resideInPackage("..domain..") }
    }

    @Test
    fun `repository implementations reside in data package`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue { it.resideInPackage("..data..") }
    }

    @Test
    fun `use cases reside in domain package`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue { it.resideInPackage("..domain..") }
    }

    @Test
    fun `dtos reside in data package`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith("Dto")
            .assertTrue { it.resideInPackage("..data..") }
    }

    @Test
    fun `domain layer classes do not use data suffixes`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .assertFalse {
                it.resideInPackage("..domain..") &&
                    (it.name.endsWith("Dto") || it.name.endsWith("Entity"))
            }
    }
}
