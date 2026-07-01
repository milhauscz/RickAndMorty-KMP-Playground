package cz.cernilovsky.kmp.rickandmorty.location.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.location.data.local.LocationEntity
import cz.cernilovsky.kmp.rickandmorty.location.data.mapper.toDomain
import cz.cernilovsky.kmp.rickandmorty.location.data.mapper.toEntity
import cz.cernilovsky.kmp.rickandmorty.location.domain.ILocationRepository
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class LocationRepository(
    private val remoteDataSource: ILocationDataSource,
    private val localDataSource: LocationRoomDataSource,
) : ILocationRepository {
    override fun observeByUrls(urls: List<String>): Flow<List<Location>> =
        localDataSource
            .locationsByUrls(urls)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshByUrls(urls: List<String>): EmptyResult<DataError.Remote> {
        val targetUrls = urls.filter { it.isNotBlank() }.distinct()
        if (targetUrls.isEmpty()) return Result.Success(Unit)

        val cachedUrls =
            localDataSource
                .locationsByUrls(targetUrls)
                .first()
                .map { it.url }
                .toSet()
        val missingUrls = targetUrls.filterNot { it in cachedUrls }

        val fetched = mutableListOf<LocationEntity>()
        missingUrls.forEach { url ->
            when (val result = remoteDataSource.getLocation(url)) {
                is Result.Error -> return Result.Error(result.error)
                is Result.Success -> fetched += result.data.toEntity()
            }
        }
        if (fetched.isNotEmpty()) localDataSource.upsertAll(fetched)
        return Result.Success(Unit)
    }
}
