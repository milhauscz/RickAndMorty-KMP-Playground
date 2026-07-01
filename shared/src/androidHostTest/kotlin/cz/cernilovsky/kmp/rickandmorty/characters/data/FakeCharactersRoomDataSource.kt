package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharactersMetadataEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCharactersRoomDataSource : CharactersRoomDataSource {
    private val _characters = mutableListOf<CharacterEntity>()
    private val _remoteKeys = mutableMapOf<Int, CharacterRemoteKeyEntity>()
    private var storedMetadata: CharactersMetadataEntity? = null

    val characters: List<CharacterEntity> get() = _characters
    val remoteKeys: List<CharacterRemoteKeyEntity> get() = _remoteKeys.values.toList()
    var refreshCallCount = 0
        private set

    fun setLastUpdated(epochMillis: Long) {
        storedMetadata = CharactersMetadataEntity(lastUpdated = epochMillis)
    }

    fun setRemoteKey(remoteKey: CharacterRemoteKeyEntity) {
        _remoteKeys[remoteKey.characterId] = remoteKey
    }

    override suspend fun insertAll(characters: List<CharacterEntity>) {
        _characters.addAll(characters)
    }

    override suspend fun insertAllRemoteKeys(remoteKeys: List<CharacterRemoteKeyEntity>) {
        remoteKeys.forEach { _remoteKeys[it.characterId] = it }
    }

    override fun pagingSource(): PagingSource<Int, CharacterEntity> =
        object : PagingSource<Int, CharacterEntity>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterEntity> =
                LoadResult.Page(data = _characters.toList(), prevKey = null, nextKey = null)

            override fun getRefreshKey(state: PagingState<Int, CharacterEntity>): Int? = null
        }

    override fun characterById(id: Int): Flow<CharacterEntity?> = flowOf(_characters.firstOrNull { it.id == id })

    override suspend fun remoteKeyByCharacterId(id: Int): CharacterRemoteKeyEntity? = _remoteKeys[id]

    override suspend fun clearAllCharacters() {
        _characters.clear()
    }

    override suspend fun clearAllRemoteKeys() {
        _remoteKeys.clear()
    }

    override suspend fun refresh(
        characters: List<CharacterEntity>,
        remoteKeys: List<CharacterRemoteKeyEntity>,
    ) {
        refreshCallCount++
        super.refresh(characters, remoteKeys)
    }

    override suspend fun getCharactersMetadata(): CharactersMetadataEntity? = storedMetadata

    override suspend fun upsertCharactersMetadata(charactersMetadataEntity: CharactersMetadataEntity) {
        storedMetadata = charactersMetadataEntity
    }
}
