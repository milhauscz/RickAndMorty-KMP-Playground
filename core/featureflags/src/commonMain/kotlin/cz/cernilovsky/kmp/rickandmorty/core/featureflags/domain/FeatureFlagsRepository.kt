package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result

internal interface FeatureFlagsRepository {
    suspend fun getCached(): Map<String, RemoteFlagConfig>

    suspend fun replaceAll(config: Map<String, RemoteFlagConfig>)

    /** Null when no remote URL was configured (not an error). */
    suspend fun fetchRemote(): Result<Map<String, RemoteFlagConfig>, DataError.Remote>?
}
