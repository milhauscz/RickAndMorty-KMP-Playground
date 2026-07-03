@file:OptIn(ExperimentalCoroutinesApi::class)

package cz.cernilovsky.kmp.rickandmorty.characters.ui.filters

import cz.cernilovsky.kmp.rickandmorty.characters.FakeCharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CharacterFiltersViewModelTest {
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
        CharacterFiltersViewModel(
            ObserveCharacterFiltersUseCase(repo),
            SetCharacterFiltersUseCase(repo),
        )

    @Test
    fun `initial state is seeded from the persisted filters`() =
        runTest(testDispatcher) {
            val repo =
                FakeCharactersRepository(
                    initialFilters = CharacterFilters(name = "rick", status = CharacterStatus.Alive),
                )
            val vm = viewModel(repo)
            backgroundScope.launch { vm.uiState.collect {} }
            runCurrent()

            assertEquals("rick", vm.uiState.value.name)
            assertEquals(CharacterStatus.Alive, vm.uiState.value.status)
        }

    @Test
    fun `onStatusSelect toggles the selection off when re-selected`() =
        runTest(testDispatcher) {
            val vm = viewModel(FakeCharactersRepository())
            backgroundScope.launch { vm.uiState.collect {} }
            runCurrent()

            vm.onStatusSelect(CharacterStatus.Dead)
            runCurrent()
            assertEquals(CharacterStatus.Dead, vm.uiState.value.status)

            vm.onStatusSelect(CharacterStatus.Dead)
            runCurrent()
            assertNull(vm.uiState.value.status)
        }

    @Test
    fun `apply persists trimmed filters and marks the state applied`() =
        runTest(testDispatcher) {
            val repo = FakeCharactersRepository()
            val vm = viewModel(repo)
            backgroundScope.launch { vm.uiState.collect {} }
            runCurrent()

            vm.onNameChange("  rick  ")
            vm.onGenderSelect(CharacterGender.Male)
            vm.apply()
            advanceUntilIdle()

            assertEquals(CharacterFilters(name = "rick", gender = CharacterGender.Male), repo.lastSetFilters)
            assertTrue(vm.uiState.value.isApplied)
        }
}
