package cz.cernilovsky.kmp.rickandmorty.episode.domain

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodeRepository {
    fun observeByUrls(urls: List<String>): Flow<List<Episode>>

    suspend fun refreshByUrls(urls: List<String>): EmptyResult<DataError.Remote>
}
