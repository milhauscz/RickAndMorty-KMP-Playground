package cz.cernilovsky.kmp.rickandmorty.runtime

import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.runtime.bridge.CharactersIosBridge
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
        val useCase = RickAndMortySdk.get<GetCharactersUseCase>()
        assertNotNull(useCase)

        RickAndMortySdk.shutdown()
    }

    @Test
    fun initialize_createsCharactersIosBridge() {
        RickAndMortySdk.initialize(RickAndMortySdkConfig.default())

        val bridge = CharactersIosBridge.create()
        assertNotNull(bridge.observeFilters())
        bridge.close()

        RickAndMortySdk.shutdown()
    }
}
