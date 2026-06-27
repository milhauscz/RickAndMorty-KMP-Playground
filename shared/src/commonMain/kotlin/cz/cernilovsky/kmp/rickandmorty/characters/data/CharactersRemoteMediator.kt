package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.mapper.toEntity
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientException
import kotlin.Int
import kotlin.OptIn
import kotlin.Result
import kotlin.collections.firstOrNull
import kotlin.collections.isNotEmpty
import kotlin.collections.lastOrNull
import kotlin.collections.map
import kotlin.collections.minus
import kotlin.collections.plus
import kotlin.let
import kotlin.map
import kotlin.plus
import kotlin.ranges.firstOrNull
import kotlin.ranges.lastOrNull
import kotlin.sequences.firstOrNull
import kotlin.sequences.lastOrNull
import kotlin.sequences.map
import kotlin.sequences.minus
import kotlin.sequences.plus
import kotlin.text.firstOrNull
import kotlin.text.isNotEmpty
import kotlin.text.lastOrNull
import kotlin.text.map
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalPagingApi::class)
class CharactersRemoteMediator(
    private val remoteDataSource: ICharactersDataSource,
    private val localDataSource: CharactersRoomDataSource,
) : RemoteMediator<Int, CharacterEntity>() {
    override suspend fun initialize(): InitializeAction {
        val lastRefresh = Instant.fromEpochMilliseconds(localDataSource.lastUpdated())
        val now = Clock.System.now()
        return if (now - lastRefresh > 15.minutes) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKey = remoteKeyClosestToCurrentPosition(state)
                remoteKey?.nextKey?.minus(1) ?: STARTING_PAGE
            }

            LoadType.PREPEND -> {
                val remoteKey = remoteKeyForFirstItem(state)
                    ?: return MediatorResult.Success(endOfPaginationReached = false)
                remoteKey.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                val remoteKey = remoteKeyForLastItem(state)
                    ?: return MediatorResult.Success(endOfPaginationReached = false)
                remoteKey.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return when (val result = remoteDataSource.getCharacters(page)) {
            is Result.Error -> MediatorResult.Error(HttpClientException(result.error))
            is Result.Success -> {
                val response = result.data
                val endOfPaginationReached = response.info.next == null
                val prevKey = if (page == STARTING_PAGE) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val characters = response.results.map { it.toEntity() }
                val remoteKeys = response.results.map { dto ->
                    CharacterRemoteKeyEntity(
                        characterId = dto.id,
                        prevKey = prevKey,
                        nextKey = nextKey,
                    )
                }

                if (loadType == LoadType.REFRESH) {
                    localDataSource.refresh(characters, remoteKeys)
                } else {
                    localDataSource.insertAll(characters)
                    localDataSource.insertAllRemoteKeys(remoteKeys)
                }
                localDataSource.updateLastUpdated()

                MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            }
        }
    }

    private suspend fun remoteKeyClosestToCurrentPosition(
        state: PagingState<Int, CharacterEntity>,
    ): CharacterRemoteKeyEntity? = state.anchorPosition?.let { position ->
        state.closestItemToPosition(position)?.id?.let { id ->
            localDataSource.remoteKeyByCharacterId(id)
        }
    }

    private suspend fun remoteKeyForFirstItem(
        state: PagingState<Int, CharacterEntity>,
    ): CharacterRemoteKeyEntity? = state.pages.firstOrNull { it.data.isNotEmpty() }
        ?.data?.firstOrNull()
        ?.let { localDataSource.remoteKeyByCharacterId(it.id) }

    private suspend fun remoteKeyForLastItem(
        state: PagingState<Int, CharacterEntity>,
    ): CharacterRemoteKeyEntity? = state.pages.lastOrNull { it.data.isNotEmpty() }
        ?.data?.lastOrNull()
        ?.let { localDataSource.remoteKeyByCharacterId(it.id) }

    private companion object {
        const val STARTING_PAGE = 1
    }
}
