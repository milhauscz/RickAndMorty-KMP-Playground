@file:OptIn(ExperimentalCoroutinesApi::class)

package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import cz.cernilovsky.kmp.rickandmorty.characters.FakeCharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.FakeEpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.characters.FakeLocationRepository
import cz.cernilovsky.kmp.rickandmorty.characters.character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharacterDetailUseCase
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CharacterDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        charsRepo: FakeCharactersRepository,
        episodeRepo: FakeEpisodeRepository = FakeEpisodeRepository(),
        locationRepo: FakeLocationRepository = FakeLocationRepository(),
    ) = CharacterDetailViewModel(
        characterId = 1,
        getCharacterDetail = GetCharacterDetailUseCase(charsRepo, locationRepo, episodeRepo),
    )

    @Test
    fun `emits the character detail and clears loading on success`() =
        runTest(testDispatcher) {
            val vm = viewModel(FakeCharactersRepository(characters = listOf(character(id = 1, name = "Rick Sanchez"))))
            backgroundScope.launch { vm.uiState.collect {} }
            advanceUntilIdle()

            val state = vm.uiState.value
            assertEquals("Rick Sanchez", state.detail?.name)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    @Test
    fun `surfaces an error message when refresh fails`() =
        runTest(testDispatcher) {
            val vm =
                viewModel(
                    charsRepo = FakeCharactersRepository(characters = listOf(character(id = 1))),
                    locationRepo = FakeLocationRepository(refreshResult = Result.Error(DataError.Remote.NO_INTERNET)),
                )
            backgroundScope.launch { vm.uiState.collect {} }
            advanceUntilIdle()

            assertNotNull(vm.uiState.value.errorMessage)
        }
}
