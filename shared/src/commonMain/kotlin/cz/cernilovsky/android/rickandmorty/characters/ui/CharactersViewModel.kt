package cz.cernilovsky.android.rickandmorty.characters.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.map
import cz.cernilovsky.android.rickandmorty.characters.domain.CharactersPagingSource
import cz.cernilovsky.android.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharactersViewModel(
    private val getCharactersUseCase: GetCharactersUseCase
) : ViewModel() {
    val charactersPagingFlow: Flow<PagingData<UiCharacter>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = true
        ),
        pagingSourceFactory = {
            CharactersPagingSource(getCharactersUseCase)
        }
    )
        .flow
        .map { pagingData ->
            pagingData.map { character ->
                // TODO - add favorites info
                character.toUiCharacter()
            }
        }
        .cachedIn(viewModelScope)
}