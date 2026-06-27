package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharactersMetadataEntity
import kotlin.time.Clock

@Dao
interface CharactersRoomDataSource {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeys(remoteKeys: List<CharacterRemoteKeyEntity>)

    @Query("SELECT * FROM characters ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, CharacterEntity>

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
    }

    @Query("SELECT * FROM characters_metadata")
    abstract suspend fun getCharactersMetadata() : CharactersMetadataEntity?

    @Upsert
    abstract suspend fun upsertCharactersMetadata(charactersMetadataEntity: CharactersMetadataEntity)

    suspend fun lastUpdated(): Long {
        return getCharactersMetadata()?.lastUpdated ?: 0
    }

    @Transaction
    suspend fun updateLastUpdated() {
        upsertCharactersMetadata(getCharactersMetadata() ?: CharactersMetadataEntity().copy(
            lastUpdated = Clock.System.now().toEpochMilliseconds()
        ))
    }
}
