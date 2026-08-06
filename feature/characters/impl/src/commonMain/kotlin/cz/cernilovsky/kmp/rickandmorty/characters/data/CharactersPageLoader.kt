package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.mapper.toDomain
import cz.cernilovsky.kmp.rickandmorty.characters.data.mapper.toEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharactersLoadType
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharactersPageLoadResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.ClearableCacheStorage
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Headless character page loader: Init / Append / Prepend with local-first read-through.
 * Not used by [CharactersRemoteMediator] (Paging only calls the mediator on cache miss).
 */
internal class CharactersPageLoader(
    private val remoteDataSource: CharactersDataSource,
    private val localDataSource: CharactersRoomDataSource,
    private val cacheStorage: ClearableCacheStorage,
    private val baseUrl: String = NetworkConfig.DEFAULT_BASE_URL,
) {
    suspend fun load(
        loadType: CharactersLoadType,
        filters: CharacterFilters,
        anchorCharacterId: Int? = null,
    ): Result<CharactersPageLoadResult, DataError.Remote> {
        persistFilters(filters)
        return when (loadType) {
            CharactersLoadType.Init -> loadInit(filters)
            CharactersLoadType.Append -> loadAppend(filters, anchorCharacterId)
            CharactersLoadType.Prepend -> loadPrepend(filters, anchorCharacterId)
        }
    }

    private suspend fun loadInit(filters: CharacterFilters): Result<CharactersPageLoadResult, DataError.Remote> {
        val refreshUrl = buildCharactersUrl(filters, baseUrl = baseUrl)
        if (shouldRefreshFromRemote(refreshUrl)) {
            cacheStorage.clear()
            return when (val result = remoteDataSource.getCharacters(refreshUrl)) {
                is Result.Error -> {
                    if (result.error == DataError.Remote.NOT_FOUND) {
                        localDataSource.refresh(emptyList(), emptyList(), refreshUrl)
                        Result.Success(emptyPage())
                    } else {
                        result
                    }
                }

                is Result.Success -> {
                    val (characters, remoteKeys) = result.data.toEntitiesAndKeys()
                    localDataSource.refresh(characters, remoteKeys, refreshUrl)
                    Result.Success(pageResult(characters.map { it.toDomain() }))
                }
            }
        }

        val entities = localDataSource.charactersFirstPage(PAGE_SIZE)
        return Result.Success(pageResult(entities.map { it.toDomain() }))
    }

    private suspend fun loadAppend(
        filters: CharacterFilters,
        anchorCharacterId: Int?,
    ): Result<CharactersPageLoadResult, DataError.Remote> {
        val anchorId =
            anchorCharacterId
                ?: return Result.Success(emptyPage())

        val localPage = localDataSource.charactersAfter(anchorId, PAGE_SIZE)
        if (localPage.isNotEmpty()) {
            return Result.Success(pageResult(localPage.map { it.toDomain() }))
        }

        val nextUrl =
            localDataSource.remoteKeyByCharacterId(anchorId)?.nextKey
                ?: return Result.Success(emptyPage())

        return fetchAndAppend(nextUrl, appliedFiltersKey(filters))
    }

    private suspend fun loadPrepend(
        filters: CharacterFilters,
        anchorCharacterId: Int?,
    ): Result<CharactersPageLoadResult, DataError.Remote> {
        val anchorId =
            anchorCharacterId
                ?: return Result.Success(emptyPage())

        val localPage = localDataSource.charactersBefore(anchorId, PAGE_SIZE).asReversed()
        if (localPage.isNotEmpty()) {
            return Result.Success(pageResult(localPage.map { it.toDomain() }))
        }

        val prevUrl =
            localDataSource.remoteKeyByCharacterId(anchorId)?.prevKey
                ?: return Result.Success(emptyPage())

        return fetchAndAppend(prevUrl, appliedFiltersKey(filters))
    }

    private suspend fun fetchAndAppend(
        url: String,
        appliedFiltersKey: String,
    ): Result<CharactersPageLoadResult, DataError.Remote> =
        when (val result = remoteDataSource.getCharacters(url)) {
            is Result.Error -> {
                if (result.error == DataError.Remote.NOT_FOUND) {
                    Result.Success(emptyPage())
                } else {
                    result
                }
            }

            is Result.Success -> {
                val (characters, remoteKeys) = result.data.toEntitiesAndKeys()
                localDataSource.append(characters, remoteKeys)
                localDataSource.updateLoadSuccess(appliedFiltersKey)
                Result.Success(pageResult(characters.map { it.toDomain() }))
            }
        }

    private suspend fun shouldRefreshFromRemote(refreshUrl: String): Boolean {
        val metadata = localDataSource.getCharactersMetadata()
        val filtersChanged = metadata?.appliedFiltersKey != refreshUrl
        val lastRefresh = Instant.fromEpochMilliseconds(metadata?.lastUpdated ?: 0)
        val now = Clock.System.now()
        return filtersChanged || now - lastRefresh > CACHE_TTL
    }

    private suspend fun persistFilters(filters: CharacterFilters) {
        localDataSource.updateSelectedFilters(
            name = filters.name,
            species = filters.species,
            type = filters.type,
            status = filters.status?.toEntity(),
            gender = filters.gender?.toEntity(),
        )
    }

    private fun appliedFiltersKey(filters: CharacterFilters): String = buildCharactersUrl(filters, baseUrl = baseUrl)

    private suspend fun pageResult(characters: List<Character>): CharactersPageLoadResult {
        if (characters.isEmpty()) {
            return emptyPage()
        }
        val firstId = characters.first().id
        val lastId = characters.last().id
        val hasMoreLocal = localDataSource.hasCharactersAfter(lastId)
        val hasPreviousLocal = localDataSource.hasCharactersBefore(firstId)
        val hasMoreRemote = localDataSource.remoteKeyByCharacterId(lastId)?.nextKey != null
        val hasPreviousRemote = localDataSource.remoteKeyByCharacterId(firstId)?.prevKey != null
        return CharactersPageLoadResult(
            characters = characters,
            hasMore = hasMoreLocal || hasMoreRemote,
            hasPrevious = hasPreviousLocal || hasPreviousRemote,
        )
    }

    private fun emptyPage(): CharactersPageLoadResult =
        CharactersPageLoadResult(
            characters = emptyList(),
            hasMore = false,
            hasPrevious = false,
        )

    private companion object {
        const val PAGE_SIZE = 20
        val CACHE_TTL = 15.minutes

        private typealias CharacterPageEntities =
            Pair<List<CharacterEntity>, List<CharacterRemoteKeyEntity>>

        private fun CharactersResponseDto.toEntitiesAndKeys(): CharacterPageEntities {
            val prevKey = info.prev
            val nextKey = info.next
            val characters = results.map { it.toEntity() }
            val remoteKeys =
                results.map { dto ->
                    CharacterRemoteKeyEntity(
                        characterId = dto.id,
                        prevKey = prevKey,
                        nextKey = nextKey,
                    )
                }
            return characters to remoteKeys
        }
    }
}
