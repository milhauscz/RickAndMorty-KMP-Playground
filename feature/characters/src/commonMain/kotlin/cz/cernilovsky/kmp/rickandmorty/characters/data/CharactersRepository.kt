package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.mapper.toDomain
import cz.cernilovsky.kmp.rickandmorty.characters.data.mapper.toEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.mapper.toFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
class CharactersRepository(
    private val remoteDataSource: ICharactersDataSource,
    private val localDataSource: CharactersRoomDataSource,
    private val applicationScope: CoroutineScope,
) : ICharactersRepository {
    // Selection for the two-pane layout, sourced directly from Room (the single source of truth):
    // set explicitly by a tap, and reset to the first character whenever the list is refreshed (see
    // CharactersRoomDataSource.refresh). stateIn keeps a hot StateFlow so `.value` is readable and
    // the Room query isn't re-run per observer.
    override val selectedCharacterId: StateFlow<Int?> =
        localDataSource
            .observeCharactersMetadata()
            .map { it?.selectedCharacterId }
            // No distinctUntilChanged: stateIn yields a StateFlow, which already conflates
            // consecutive equal values, so duplicates from unrelated metadata writes are dropped.
            .stateIn(applicationScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS), null)

    override val filters: Flow<CharacterFilters> =
        localDataSource
            .observeCharactersMetadata()
            .map { it.toFilters() }
            .distinctUntilChanged()

    // Declared after `filters` because it derives from it; a val initializer can only read
    // properties already initialized above it.
    override val charactersPagingData: Flow<PagingData<Character>> =
        filters
            // The mediator writes to characters_metadata on every page load, so without
            // distinctUntilChanged upstream, each page write would re-trigger flatMapLatest
            // and recreate the Pager in an infinite refresh loop.
            .flatMapLatest { activeFilters ->
                // See onLocalDataChanged in CharactersRemoteMediator: Room can lose the
                // invalidation for the mediator's own refresh write, so the mediator invalidates
                // the newest PagingSource explicitly after each successful write.
                var currentPagingSource: PagingSource<Int, CharacterEntity>? = null
                Pager(
                    config =
                        PagingConfig(
                            pageSize = PAGE_SIZE,
                            enablePlaceholders = false,
                        ),
                    remoteMediator =
                        CharactersRemoteMediator(
                            remoteDataSource,
                            localDataSource,
                            activeFilters,
                            onLocalDataChanged = { currentPagingSource?.invalidate() },
                        ),
                    pagingSourceFactory = {
                        localDataSource.pagingSource().also { currentPagingSource = it }
                    },
                ).flow
            }.map { pagingData ->
                pagingData.map { entity -> entity.toDomain() }
            }

    override fun observeCharacter(id: Int): Flow<Character?> =
        localDataSource
            .characterById(id)
            .map { entity -> entity?.toDomain() }

    override suspend fun setFilters(filters: CharacterFilters) {
        localDataSource.updateSelectedFilters(
            name = filters.name,
            species = filters.species,
            type = filters.type,
            status = filters.status?.toEntity(),
            gender = filters.gender?.toEntity(),
        )
    }

    override suspend fun setSelectedCharacterId(id: Int?) {
        localDataSource.updateSelectedCharacterId(id)
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
    }
}
