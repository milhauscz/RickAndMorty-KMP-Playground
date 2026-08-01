package cz.cernilovsky.kmp.rickandmorty.characters.domain

import androidx.paging.PagingData
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import kotlinx.coroutines.flow.Flow

public interface CharactersRepository {
    /**
     * The character selected in the two-pane list/detail layout, or null when nothing is selected.
     * Cleared automatically whenever the cached list is refreshed (see [charactersPagingData]),
     * since a selection made against the previous list no longer applies to the new one.
     */
    public val selectedCharacterId: Flow<Int?>

    public val filters: Flow<CharacterFilters>

    public val charactersPagingData: Flow<PagingData<Character>>

    public fun observeCharacter(id: Int): Flow<Character?>

    public suspend fun setFilters(filters: CharacterFilters)

    public suspend fun setSelectedCharacterId(id: Int?)
}
