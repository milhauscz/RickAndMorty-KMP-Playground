package cz.cernilovsky.kmp.rickandmorty.episode.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.episode.data.mapper.toDomain
import cz.cernilovsky.kmp.rickandmorty.episode.data.mapper.toEntity
import cz.cernilovsky.kmp.rickandmorty.episode.domain.IEpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class EpisodeRepository(
    private val remoteDataSource: IEpisodeDataSource,
    private val localDataSource: EpisodeRoomDataSource,
) : IEpisodeRepository {
    override fun observeByUrls(urls: List<String>): Flow<List<Episode>> =
        localDataSource
            .episodesByUrls(urls)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshByUrls(urls: List<String>): EmptyResult<DataError.Remote> {
        val targetUrls = urls.filter { it.isNotBlank() }.distinct()
        if (targetUrls.isEmpty()) return Result.Success(Unit)

        val cachedUrls =
            localDataSource
                .episodesByUrls(targetUrls)
                .first()
                .map { it.url }
                .toSet()
        val missingIds = targetUrls.filterNot { it in cachedUrls }.mapNotNull { it.toEpisodeId() }
        if (missingIds.isEmpty()) return Result.Success(Unit)

        return when (val result = remoteDataSource.getEpisodes(missingIds)) {
            is Result.Error -> {
                Result.Error(result.error)
            }

            is Result.Success -> {
                localDataSource.upsertAll(result.data.map { it.toEntity() })
                Result.Success(Unit)
            }
        }
    }

    private fun String.toEpisodeId(): Int? = substringAfterLast('/').toIntOrNull()
}
