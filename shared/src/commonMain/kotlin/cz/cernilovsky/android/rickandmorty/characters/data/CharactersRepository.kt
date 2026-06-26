package cz.cernilovsky.android.rickandmorty.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import cz.cernilovsky.android.rickandmorty.characters.data.mapper.toDomain
import cz.cernilovsky.android.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.android.rickandmorty.characters.domain.model.Character
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalPagingApi::class)
class CharactersRepository(
    private val remoteDataSource: ICharactersDataSource,
    private val localDataSource: CharactersRoomDataSource,
) : ICharactersRepository {
    override fun getCharactersPagingData(): Flow<PagingData<Character>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
        ),
        remoteMediator = CharactersRemoteMediator(remoteDataSource, localDataSource),
        pagingSourceFactory = { localDataSource.pagingSource() },
    )
        .flow
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
