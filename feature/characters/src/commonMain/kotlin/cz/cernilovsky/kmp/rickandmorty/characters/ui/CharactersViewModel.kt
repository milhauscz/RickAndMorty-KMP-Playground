package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilterField
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val FILTERS_SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

class CharactersViewModel(
    getCharactersUseCase: GetCharactersUseCase,
    observeCharacterFiltersUseCase: ObserveCharacterFiltersUseCase,
    private val setCharacterFiltersUseCase: SetCharacterFiltersUseCase,
) : ViewModel() {
    val charactersPagingFlow: Flow<PagingData<UiCharacter>> =
        getCharactersUseCase()
            .map { pagingData ->
                pagingData.map { character ->
                    character.toUiCharacter()
                }
            }.cachedIn(viewModelScope)

    val filters: StateFlow<CharacterFilters> =
        observeCharacterFiltersUseCase()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(FILTERS_SUBSCRIPTION_TIMEOUT_MILLIS),
                CharacterFilters.EMPTY,
            )

    fun removeFilter(field: CharacterFilterField) {
        viewModelScope.launch {
            setCharacterFiltersUseCase(filters.value.without(field))
        }
    }

    fun clearFilters() {
        viewModelScope.launch {
            setCharacterFiltersUseCase(CharacterFilters.EMPTY)
        }
    }
}
