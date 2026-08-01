package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlags
import cz.cernilovsky.kmp.rickandmorty.episode.domain.EpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import cz.cernilovsky.kmp.rickandmorty.location.domain.LocationRepository
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFeatureFlags(
    private val overrides: Map<String, Boolean> = emptyMap(),
) : FeatureFlags {
    override fun isEnabled(flag: FeatureFlag): Boolean = overrides[flag.key] ?: flag.defaultEnabled

    override suspend fun refresh(): EmptyResult<DataError.Remote> = Result.Success(Unit)
}

class CountingEpisodeRepository : EpisodeRepository {
    var refreshCount: Int = 0
        private set

    override fun observeByUrls(urls: List<String>): Flow<List<Episode>> = flowOf(emptyList())

    override suspend fun refreshByUrls(urls: List<String>): EmptyResult<DataError.Remote> {
        refreshCount++
        return Result.Success(Unit)
    }
}

class CountingLocationRepository : LocationRepository {
    var refreshCount: Int = 0
        private set

    override fun observeByUrls(urls: List<String>): Flow<List<Location>> = flowOf(emptyList())

    override suspend fun refreshByUrls(urls: List<String>): EmptyResult<DataError.Remote> {
        refreshCount++
        return Result.Success(Unit)
    }
}
