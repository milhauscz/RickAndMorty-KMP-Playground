package cz.cernilovsky.kmp.rickandmorty.characters.domain

import androidx.paging.PagingData
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface ICharactersRepository {
    fun getCharactersPagingData(): Flow<PagingData<Character>>

    fun observeCharacter(id: Int): Flow<Character?>
}
