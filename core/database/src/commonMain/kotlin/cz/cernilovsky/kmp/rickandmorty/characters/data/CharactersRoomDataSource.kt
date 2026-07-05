package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterGenderEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterStatusEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharactersMetadataEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

@Dao
interface CharactersRoomDataSource {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeys(remoteKeys: List<CharacterRemoteKeyEntity>)

    @Query("SELECT * FROM characters ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, CharacterEntity>

    @Query("SELECT * FROM characters WHERE id = :id")
    fun characterById(id: Int): Flow<CharacterEntity?>

    @Query("SELECT * FROM character_remote_keys WHERE characterId = :id")
    suspend fun remoteKeyByCharacterId(id: Int): CharacterRemoteKeyEntity?

    @Query("DELETE FROM characters")
    suspend fun clearAllCharacters()

    @Query("DELETE FROM character_remote_keys")
    suspend fun clearAllRemoteKeys()

    @Transaction
    suspend fun refresh(
        characters: List<CharacterEntity>,
        remoteKeys: List<CharacterRemoteKeyEntity>,
    ) {
        clearAllCharacters()
        clearAllRemoteKeys()
        insertAll(characters)
        insertAllRemoteKeys(remoteKeys)
        // The previous selection referred to a list that no longer exists: point it at the first
        // character of the fresh list, or clear it when the list is empty. Done here (atomically
        // with the wipe) so observers of the metadata never see a selection that isn't in the list.
        updateSelectedCharacterId(characters.firstOrNull()?.id)
    }

    /**
     * Replaces the cached characters and records the applied-filters key in one transaction so the
     * key always reflects the data actually in the table. Doing these as two separate writes lets a
     * refresh cancelled mid-way by a rapid filter change leave new-filter rows tagged with the
     * previous key, which makes the next load wrongly skip its initial refresh.
     */
    @Transaction
    suspend fun refresh(
        characters: List<CharacterEntity>,
        remoteKeys: List<CharacterRemoteKeyEntity>,
        appliedFiltersKey: String,
    ) {
        refresh(characters, remoteKeys)
        updateLoadSuccess(appliedFiltersKey)
    }

    @Transaction
    suspend fun append(
        characters: List<CharacterEntity>,
        remoteKeys: List<CharacterRemoteKeyEntity>,
    ) {
        insertAll(characters)
        insertAllRemoteKeys(remoteKeys)
    }

    @Query("SELECT * FROM characters_metadata")
    suspend fun getCharactersMetadata(): CharactersMetadataEntity?

    @Query("SELECT * FROM characters_metadata")
    fun observeCharactersMetadata(): Flow<CharactersMetadataEntity?>

    @Upsert
    suspend fun upsertCharactersMetadata(charactersMetadataEntity: CharactersMetadataEntity)

    suspend fun lastUpdated(): Long = getCharactersMetadata()?.lastUpdated ?: 0

    @Transaction
    suspend fun updateLoadSuccess(appliedFiltersKey: String) {
        val current = getCharactersMetadata() ?: CharactersMetadataEntity()
        upsertCharactersMetadata(
            current.copy(
                lastUpdated = Clock.System.now().toEpochMilliseconds(),
                appliedFiltersKey = appliedFiltersKey,
            ),
        )
    }

    @Transaction
    suspend fun updateSelectedCharacterId(id: Int?) {
        val current = getCharactersMetadata() ?: CharactersMetadataEntity()
        upsertCharactersMetadata(current.copy(selectedCharacterId = id))
    }

    @Transaction
    suspend fun updateSelectedFilters(
        name: String?,
        species: String?,
        type: String?,
        status: CharacterStatusEntity?,
        gender: CharacterGenderEntity?,
    ) {
        val current = getCharactersMetadata() ?: CharactersMetadataEntity()
        upsertCharactersMetadata(
            current.copy(
                filterName = name,
                filterSpecies = species,
                filterType = type,
                filterStatus = status,
                filterGender = gender,
            ),
        )
    }
}
