package cz.cernilovsky.kmp.rickandmorty.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import org.junit.jupiter.api.Test

class LayerArchitectureTest {
    @Test
    fun `clean architecture layers have correct dependencies`() {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                val domain = Layer("Domain", "cz.cernilovsky.kmp.rickandmorty..domain..")
                val data = Layer("Data", "cz.cernilovsky.kmp.rickandmorty..data..")
                val ui = Layer("UI", "cz.cernilovsky.kmp.rickandmorty..ui..")
                val di = Layer("DI", "cz.cernilovsky.kmp.rickandmorty..di..")

                domain.dependsOnNothing()
                data.dependsOn(domain)
                ui.dependsOn(domain)
                di.dependsOn(domain)
                di.dependsOn(data)
            }
    }
}
