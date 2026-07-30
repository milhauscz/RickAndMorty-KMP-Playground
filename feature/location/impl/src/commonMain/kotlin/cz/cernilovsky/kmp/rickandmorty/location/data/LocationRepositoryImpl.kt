package cz.cernilovsky.kmp.rickandmorty.location.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.location.data.mapper.toDomain
import cz.cernilovsky.kmp.rickandmorty.location.data.mapper.toEntity
import cz.cernilovsky.kmp.rickandmorty.location.domain.LocationRepository
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class LocationRepositoryImpl(
    private val remoteDataSource: LocationDataSource,
    private val localDataSource: LocationRoomDataSource,
) : LocationRepository {
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
        val missingIds = targetUrls.filterNot { it in cachedUrls }.mapNotNull { it.toLocationId() }
        if (missingIds.isEmpty()) return Result.Success(Unit)

        return when (val result = remoteDataSource.getLocations(missingIds)) {
            is Result.Error -> {
                Result.Error(result.error)
            }

            is Result.Success -> {
                localDataSource.upsertAll(result.data.map { it.toEntity() })
                Result.Success(Unit)
            }
        }
    }

    private fun String.toLocationId(): Int? = substringAfterLast('/').toIntOrNull()
}
