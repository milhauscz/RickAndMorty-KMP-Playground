package cz.cernilovsky.kmp.rickandmorty.characters.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharacterFiltersViewModel(
    private val observeCharacterFiltersUseCase: ObserveCharacterFiltersUseCase,
    private val setCharacterFiltersUseCase: SetCharacterFiltersUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CharacterFiltersUiState())
    val uiState: StateFlow<CharacterFiltersUiState> = _uiState
        .onStart {
            initUiState()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CharacterFiltersUiState(),
        )

    private suspend fun initUiState() {
        val filters = observeCharacterFiltersUseCase().first()
        _uiState.update { filters.toUiState() }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onSpeciesChange(value: String) {
        _uiState.update { it.copy(species = value) }
    }

    fun onTypeChange(value: String) {
        _uiState.update { it.copy(type = value) }
    }

    fun onStatusSelect(status: CharacterStatus) {
        _uiState.update { it.copy(status = if (it.status == status) null else status) }
    }

    fun onGenderSelect(gender: CharacterGender) {
        _uiState.update { it.copy(gender = if (it.gender == gender) null else gender) }
    }

    fun apply() {
        viewModelScope.launch {
            setCharacterFiltersUseCase(_uiState.value.toFilters())
            _uiState.update { it.copy(isApplied = true) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
