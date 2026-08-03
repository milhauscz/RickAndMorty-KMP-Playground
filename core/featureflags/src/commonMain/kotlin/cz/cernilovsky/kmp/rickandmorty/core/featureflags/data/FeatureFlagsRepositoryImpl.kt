package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.local.FeatureFlagConfigEntity
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlagsRepository
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig

internal class FeatureFlagsRepositoryImpl(
    private val localDataSource: FeatureFlagsRoomDataSource,
    private val remoteDataSource: FeatureFlagsDataSource?,
) : FeatureFlagsRepository {
    override suspend fun getCached(): Map<String, RemoteFlagConfig> =
        localDataSource.getAll().associate { entity ->
            entity.key to
                RemoteFlagConfig(
                    enabled = entity.enabled,
                    rolloutPercent = entity.rolloutPercent,
                )
        }

    override suspend fun replaceAll(config: Map<String, RemoteFlagConfig>) {
        localDataSource.replaceAll(
            config.map { (key, value) ->
                FeatureFlagConfigEntity(
                    key = key,
                    enabled = value.enabled,
                    rolloutPercent = value.rolloutPercent,
                )
            },
        )
    }

    override suspend fun fetchRemote(): Result<Map<String, RemoteFlagConfig>, DataError.Remote>? =
        remoteDataSource?.fetch()
}
