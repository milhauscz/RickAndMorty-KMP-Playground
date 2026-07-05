package cz.cernilovsky.kmp.rickandmorty.characters.domain

import androidx.paging.PagingData
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ICharactersRepository {
    /**
     * The character selected in the two-pane list/detail layout, or null when nothing is selected.
     * Cleared automatically whenever the cached list is refreshed (see [charactersPagingData]),
     * since a selection made against the previous list no longer applies to the new one.
     */
    val selectedCharacterId: StateFlow<Int?>

    val filters: Flow<CharacterFilters>

    val charactersPagingData: Flow<PagingData<Character>>

    fun observeCharacter(id: Int): Flow<Character?>

    suspend fun setFilters(filters: CharacterFilters)

    suspend fun setSelectedCharacterId(id: Int?)
}
