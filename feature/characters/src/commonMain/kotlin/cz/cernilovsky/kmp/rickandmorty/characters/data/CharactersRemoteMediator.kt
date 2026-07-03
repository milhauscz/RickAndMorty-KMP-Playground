package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.mapper.toEntity
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalPagingApi::class)
class CharactersRemoteMediator(
    private val remoteDataSource: ICharactersDataSource,
    private val localDataSource: CharactersRoomDataSource,
    filters: CharacterFilters = CharacterFilters.EMPTY,
) : RemoteMediator<Int, CharacterEntity>() {
    private val refreshUrl = buildCharactersUrl(filters)

    override suspend fun initialize(): InitializeAction {
        val metadata = localDataSource.getCharactersMetadata()
        val filtersChanged = metadata?.appliedFiltersKey != refreshUrl
        val lastRefresh = Instant.fromEpochMilliseconds(metadata?.lastUpdated ?: 0)
        val now = Clock.System.now()
        return if (filtersChanged || now - lastRefresh > 15.minutes) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>,
    ): MediatorResult {
        val url =
            when (loadType) {
                LoadType.REFRESH -> {
                    refreshUrl
                }

                LoadType.PREPEND -> {
                    val remoteKey =
                        remoteKeyForFirstItem(state)
                            ?: return MediatorResult.Success(endOfPaginationReached = false)
                    remoteKey.prevKey
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val remoteKey =
                        remoteKeyForLastItem(state)
                            ?: return MediatorResult.Success(endOfPaginationReached = false)
                    remoteKey.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

        return when (val result = remoteDataSource.getCharacters(url)) {
            is Result.Error -> {
                if (result.error == DataError.Remote.NOT_FOUND) {
                    // API returns 404 for queries which have 0 results
                    // => map to empty success instead of showing error
                    // if this was a refresh
                    if (loadType == LoadType.REFRESH) {
                        localDataSource.refresh(emptyList(), emptyList())
                        localDataSource.updateLoadSuccess(refreshUrl)
                    }
                    MediatorResult.Success(endOfPaginationReached = true)
                } else {
                    MediatorResult.Error(HttpClientException(result.error))
                }
            }

            is Result.Success -> {
                val response = result.data
                val endOfPaginationReached = response.info.next == null
                val prevKey = response.info.prev
                val nextKey = response.info.next

                val characters = response.results.map { it.toEntity() }
                val remoteKeys =
                    response.results.map { dto ->
                        CharacterRemoteKeyEntity(
                            characterId = dto.id,
                            prevKey = prevKey,
                            nextKey = nextKey,
                        )
                    }

                if (loadType == LoadType.REFRESH) {
                    localDataSource.refresh(characters, remoteKeys)
                } else {
                    localDataSource.append(characters, remoteKeys)
                }
                localDataSource.updateLoadSuccess(refreshUrl)

                MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            }
        }
    }

    private suspend fun remoteKeyForFirstItem(state: PagingState<Int, CharacterEntity>): CharacterRemoteKeyEntity? =
        state.pages
            .firstOrNull { it.data.isNotEmpty() }
            ?.data
            ?.firstOrNull()
            ?.let { localDataSource.remoteKeyByCharacterId(it.id) }

    private suspend fun remoteKeyForLastItem(state: PagingState<Int, CharacterEntity>): CharacterRemoteKeyEntity? =
        state.pages
            .lastOrNull { it.data.isNotEmpty() }
            ?.data
            ?.lastOrNull()
            ?.let { localDataSource.remoteKeyByCharacterId(it.id) }
}
