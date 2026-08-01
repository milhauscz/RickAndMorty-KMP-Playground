package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.EmptyResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.domain.asEmptyDataResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.onSuccess
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlags
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.rolloutBucket
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Resolves a flag through three sources, in a fixed order:
 *
 * 1. **A host override.** Absolute, including over a kill switch — an integrator asking for a
 *    behaviour in their own test gets it, and the alternative is them mocking the SDK entirely.
 * 2. **The remote configuration**, gated by the rollout bucket.
 * 3. **The compile-time default**, which is also what applies before the first successful refresh
 *    and on every launch without a network. There is no state in which a flag has no answer.
 */
internal class DefaultFeatureFlags(
    private val installId: String,
    private val overrides: Map<String, Boolean> = emptyMap(),
    private val dataSource: FeatureFlagsDataSource? = null,
) : FeatureFlags {
    private val remoteConfig = MutableStateFlow<Map<String, RemoteFlagConfig>>(emptyMap())

    override fun isEnabled(flag: FeatureFlag): Boolean {
        overrides[flag.key]?.let { return it }

        val remote = remoteConfig.value[flag.key] ?: return flag.defaultEnabled
        if (!remote.enabled) return false
        return rolloutBucket(installId, flag.key) < remote.rolloutPercent
    }

    override suspend fun refresh(): EmptyResult<DataError.Remote> {
        // No remote source configured is not a failure: flags then mean their compile-time defaults,
        // which is the normal state for a host that never wanted remote configuration.
        val source = dataSource ?: return Result.Success(Unit)

        return source
            .fetch()
            .onSuccess { config -> remoteConfig.value = config }
            .asEmptyDataResult()
    }
}
