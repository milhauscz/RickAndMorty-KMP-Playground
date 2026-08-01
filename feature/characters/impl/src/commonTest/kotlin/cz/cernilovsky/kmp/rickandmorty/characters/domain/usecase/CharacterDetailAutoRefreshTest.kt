package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.FakeCharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterDetail
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The behaviour behind `character_detail_auto_refresh`, with the flag both on and off.
 *
 * Both cases are tested because a flag is a promise about two code paths, and the one that is not
 * exercised is the one shipped to everyone who has not been rolled out yet.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailAutoRefreshTest {
    private val charactersRepository = FakeCharactersRepository(characters = listOf(character(id = 1)))
    private val locationRepository = CountingLocationRepository()
    private val episodeRepository = CountingEpisodeRepository()

    private fun useCaseWithAutoRefresh(enabled: Boolean): GetCharacterDetailUseCase =
        GetCharacterDetailUseCase(
            charactersRepository = charactersRepository,
            locationRepository = locationRepository,
            episodeRepository = episodeRepository,
            featureFlags =
                FakeFeatureFlags(
                    overrides = mapOf(FeatureFlag.CharacterDetailAutoRefresh.key to enabled),
                ),
        )

    @Test
    fun `with the flag off the detail is served from the cache only`() =
        runTest {
            val received = mutableListOf<CharacterDetail?>()
            val job = launch { useCaseWithAutoRefresh(enabled = false).observe(id = 1).collect { received += it } }
            advanceUntilIdle()

            assertNotNull(received.firstOrNull(), "the cached detail should still be emitted")
            assertEquals(0, locationRepository.refreshCount, "the flag is off; nothing should be fetched")
            assertEquals(0, episodeRepository.refreshCount, "the flag is off; nothing should be fetched")

            job.cancel()
        }

    @Test
    fun `with the flag on the cached detail arrives and a refresh runs alongside it`() =
        runTest {
            val received = mutableListOf<CharacterDetail?>()
            val job = launch { useCaseWithAutoRefresh(enabled = true).observe(id = 1).collect { received += it } }
            advanceUntilIdle()

            // Both halves matter: the refresh happens, and it does not replace or delay the local
            // read, so an offline consumer sees exactly what it saw before the flag existed.
            assertNotNull(received.firstOrNull(), "the cached detail must not wait for the network")
            assertEquals(1, locationRepository.refreshCount)
            assertEquals(1, episodeRepository.refreshCount)

            job.cancel()
        }

    @Test
    fun `the flag defaults to off when the host says nothing`() =
        runTest {
            val useCase =
                GetCharacterDetailUseCase(
                    charactersRepository = charactersRepository,
                    locationRepository = locationRepository,
                    episodeRepository = episodeRepository,
                    featureFlags = FakeFeatureFlags(),
                )

            val job = launch { useCase.observe(id = 1).collect { } }
            advanceUntilIdle()

            assertEquals(0, episodeRepository.refreshCount)

            job.cancel()
        }
}
