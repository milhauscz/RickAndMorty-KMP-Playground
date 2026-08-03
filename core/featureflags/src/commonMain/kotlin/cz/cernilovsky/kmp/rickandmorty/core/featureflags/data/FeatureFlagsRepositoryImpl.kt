package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.domain.asEmptyDataResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.onSuccess
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.local.FeatureFlagConfigEntity
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlagsRepository
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.rolloutBucket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single source of truth for flag resolution: Room-backed remote config as a [StateFlow], with
 * host overrides and compile-time defaults applied in [isEnabled].
 *
 * [SharingStarted.Eagerly] is intentional — nothing collects this flow for UI; [isEnabled] reads
 * [StateFlow.value] synchronously, so sharing must start as soon as the repository is constructed.
 */
internal class FeatureFlagsRepositoryImpl(
    private val localDataSource: FeatureFlagsRoomDataSource,
    private val remoteDataSource: FeatureFlagsDataSource?,
    private val installId: String,
    private val overrides: Map<String, Boolean> = emptyMap(),
    private val scope: CoroutineScope,
) : FeatureFlagsRepository {
    private val remoteConfig: StateFlow<Map<String, RemoteFlagConfig>> =
        localDataSource
            .observeAll()
            .map { entities -> entities.toRemoteConfigMap() }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap(),
            )

    init {
        // Room emits the disk cache first; this refresh then overwrites it when the network works.
        scope.launch { refresh() }
    }

    override fun isEnabled(flag: FeatureFlag): Boolean {
        // Overrides stay out of the Room flow: they are absolute booleans, not RemoteFlagConfig.
        overrides[flag.key]?.let { return it }

        val remote = remoteConfig.value[flag.key] ?: return flag.defaultEnabled
        if (!remote.enabled) return false
        return rolloutBucket(installId, flag.key) < remote.rolloutPercent
    }

    override suspend fun refresh(): EmptyResult<DataError.Remote> {
        val remoteResult = remoteDataSource?.fetch() ?: return Result.Success(Unit)

        return remoteResult
            .onSuccess { config ->
                localDataSource.replaceAll(
                    config.map { (key, value) ->
                        FeatureFlagConfigEntity(
                            key = key,
                            enabled = value.enabled,
                            rolloutPercent = value.rolloutPercent,
                        )
                    },
                )
            }.asEmptyDataResult()
    }
}

private fun List<FeatureFlagConfigEntity>.toRemoteConfigMap(): Map<String, RemoteFlagConfig> =
    associate { entity ->
        entity.key to
            RemoteFlagConfig(
                enabled = entity.enabled,
                rolloutPercent = entity.rolloutPercent,
            )
    }
