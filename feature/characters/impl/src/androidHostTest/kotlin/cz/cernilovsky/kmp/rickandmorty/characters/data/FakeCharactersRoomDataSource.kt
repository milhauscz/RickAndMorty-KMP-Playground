package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharactersMetadataEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeCharactersRoomDataSource : CharactersRoomDataSource {
    private val _characters = mutableListOf<CharacterEntity>()
    private val _remoteKeys = mutableMapOf<Int, CharacterRemoteKeyEntity>()
    private val storedMetadataFlow = MutableStateFlow<CharactersMetadataEntity?>(null)
    private var storedMetadata: CharactersMetadataEntity?
        get() = storedMetadataFlow.value
        set(value) {
            storedMetadataFlow.value = value
        }

    val characters: List<CharacterEntity> get() = _characters
    val remoteKeys: List<CharacterRemoteKeyEntity> get() = _remoteKeys.values.toList()
    val metadata: CharactersMetadataEntity? get() = storedMetadata
    var refreshCallCount = 0

    fun setLastUpdated(epochMillis: Long) {
        storedMetadata = (storedMetadata ?: CharactersMetadataEntity()).copy(lastUpdated = epochMillis)
    }

    fun setAppliedFiltersKey(key: String?) {
        storedMetadata = (storedMetadata ?: CharactersMetadataEntity()).copy(appliedFiltersKey = key)
    }

    fun setRemoteKey(remoteKey: CharacterRemoteKeyEntity) {
        _remoteKeys[remoteKey.characterId] = remoteKey
    }

    override suspend fun insertAll(characters: List<CharacterEntity>) {
        characters.forEach { incoming ->
            val index = _characters.indexOfFirst { it.id == incoming.id }
            if (index >= 0) {
                _characters[index] = incoming
            } else {
                _characters.add(incoming)
            }
        }
        _characters.sortBy { it.id }
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

    override suspend fun charactersFirstPage(limit: Int): List<CharacterEntity> =
        _characters.sortedBy { it.id }.take(limit)

    override suspend fun charactersAfter(
        afterId: Int,
        limit: Int,
    ): List<CharacterEntity> = _characters.filter { it.id > afterId }.sortedBy { it.id }.take(limit)

    override suspend fun charactersBefore(
        beforeId: Int,
        limit: Int,
    ): List<CharacterEntity> = _characters.filter { it.id < beforeId }.sortedByDescending { it.id }.take(limit)

    override suspend fun hasCharactersAfter(afterId: Int): Boolean = _characters.any { it.id > afterId }

    override suspend fun hasCharactersBefore(beforeId: Int): Boolean = _characters.any { it.id < beforeId }

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

    override fun observeCharactersMetadata(): Flow<CharactersMetadataEntity?> = storedMetadataFlow

    override suspend fun upsertCharactersMetadata(charactersMetadataEntity: CharactersMetadataEntity) {
        storedMetadata = charactersMetadataEntity
    }
}
