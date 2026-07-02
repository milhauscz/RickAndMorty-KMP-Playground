package cz.cernilovsky.kmp.rickandmorty.characters.domain

import androidx.paging.PagingData
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import kotlinx.coroutines.flow.Flow

interface ICharactersRepository {
    fun getCharactersPagingData(): Flow<PagingData<Character>>

    fun observeCharacter(id: Int): Flow<Character?>

    fun observeFilters(): Flow<CharacterFilters>

    suspend fun setFilters(filters: CharacterFilters)
}
