package cz.cernilovsky.kmp.rickandmorty.episode.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.episode.data.remote.EpisodeDto

interface EpisodeDataSource {
    suspend fun getEpisodes(ids: List<Int>): Result<List<EpisodeDto>, DataError.Remote>
}
