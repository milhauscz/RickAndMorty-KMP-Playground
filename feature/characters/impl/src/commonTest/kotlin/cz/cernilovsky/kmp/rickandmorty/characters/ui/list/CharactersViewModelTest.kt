@file:OptIn(ExperimentalCoroutinesApi::class)

package cz.cernilovsky.kmp.rickandmorty.characters.ui.list

import cz.cernilovsky.kmp.rickandmorty.characters.FakeCharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilterField
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.ui.CharactersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CharactersViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repo: FakeCharactersRepository) =
        CharactersViewModel(
            GetCharactersUseCase(repo),
            ObserveCharacterFiltersUseCase(repo),
            SetCharacterFiltersUseCase(repo),
            ObserveSelectedCharacterIdUseCase(repo),
            SetSelectedCharacterIdUseCase(repo),
        )

    @Test
    fun `filters reflect the repository`() =
        runTest(testDispatcher) {
            val repo = FakeCharactersRepository(initialFilters = CharacterFilters(name = "rick"))
            val vm = viewModel(repo)

            backgroundScope.launch { vm.filters.collect {} }
            runCurrent()

            assertEquals("rick", vm.filters.value.name)
        }

    @Test
    fun `removeFilter clears only that field`() =
        runTest(testDispatcher) {
            val repo =
                FakeCharactersRepository(
                    initialFilters = CharacterFilters(name = "rick", status = CharacterStatus.Alive),
                )
            val vm = viewModel(repo)
            backgroundScope.launch { vm.filters.collect {} }
            runCurrent()

            vm.removeFilter(CharacterFilterField.Name)
            advanceUntilIdle()

            assertEquals(CharacterFilters(status = CharacterStatus.Alive), repo.lastSetFilters)
        }

    @Test
    fun `clearFilters resets to EMPTY`() =
        runTest(testDispatcher) {
            val repo = FakeCharactersRepository(initialFilters = CharacterFilters(name = "rick"))
            val vm = viewModel(repo)
            backgroundScope.launch { vm.filters.collect {} }
            runCurrent()

            vm.clearFilters()
            advanceUntilIdle()

            assertEquals(CharacterFilters.EMPTY, repo.lastSetFilters)
        }
}
