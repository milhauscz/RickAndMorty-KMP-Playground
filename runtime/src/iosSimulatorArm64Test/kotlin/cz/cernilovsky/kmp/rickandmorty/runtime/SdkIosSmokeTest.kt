package cz.cernilovsky.kmp.rickandmorty.runtime

import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SdkIosSmokeTest {
    @AfterTest
    fun tearDown() {
        RickAndMortySdk.shutdown()
    }

    @Test
    fun initialize_resolvesHeadlessUseCase() {
        RickAndMortySdk.initialize(RickAndMortySdkConfig.default())

        assertTrue(RickAndMortySdk.isInitialized)
        val useCase = requireNotNull(RickAndMortySdk.containerOrNull).koin.get<GetCharactersUseCase>()
        assertNotNull(useCase)

        RickAndMortySdk.shutdown()
    }
}
