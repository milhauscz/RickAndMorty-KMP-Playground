package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharactersViewModel(
    getCharactersUseCase: GetCharactersUseCase
) : ViewModel() {
    val charactersPagingFlow: Flow<PagingData<UiCharacter>> = getCharactersUseCase()
        .map { pagingData ->
            pagingData.map { character ->
                // TODO - add favorites info
                character.toUiCharacter()
            }
        }
        .cachedIn(viewModelScope)
}
