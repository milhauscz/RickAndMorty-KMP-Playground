package cz.cernilovsky.kmp.rickandmorty.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import cz.cernilovsky.kmp.rickandmorty.characters.data.mapper.toDomain
import cz.cernilovsky.kmp.rickandmorty.characters.data.mapper.toFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
class CharactersRepository(
    private val remoteDataSource: ICharactersDataSource,
    private val localDataSource: CharactersRoomDataSource,
) : ICharactersRepository {
    override fun getCharactersPagingData(): Flow<PagingData<Character>> =
        observeFilters()
            // The mediator writes to characters_metadata on every page load, so without
            // distinctUntilChanged upstream, each page write would re-trigger flatMapLatest
            // and recreate the Pager in an infinite refresh loop.
            .flatMapLatest { filters ->
                Pager(
                    config =
                        PagingConfig(
                            pageSize = PAGE_SIZE,
                            enablePlaceholders = false,
                        ),
                    remoteMediator = CharactersRemoteMediator(remoteDataSource, localDataSource, filters),
                    pagingSourceFactory = { localDataSource.pagingSource() },
                ).flow
            }.map { pagingData ->
                pagingData.map { entity -> entity.toDomain() }
            }

    override fun observeCharacter(id: Int): Flow<Character?> =
        localDataSource
            .characterById(id)
            .map { entity -> entity?.toDomain() }

    override fun observeFilters(): Flow<CharacterFilters> =
        localDataSource
            .observeCharactersMetadata()
            .map { it.toFilters() }
            .distinctUntilChanged()

    override suspend fun setFilters(filters: CharacterFilters) {
        localDataSource.updateSelectedFilters(
            name = filters.name,
            species = filters.species,
            type = filters.type,
            status = filters.status?.name,
            gender = filters.gender?.name,
        )
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
