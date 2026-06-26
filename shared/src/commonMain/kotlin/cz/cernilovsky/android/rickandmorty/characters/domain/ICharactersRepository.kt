package cz.cernilovsky.android.rickandmorty.characters.domain

import androidx.paging.PagingData
import cz.cernilovsky.android.rickandmorty.characters.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface ICharactersRepository {
    fun getCharactersPagingData(): Flow<PagingData<Character>>
}
