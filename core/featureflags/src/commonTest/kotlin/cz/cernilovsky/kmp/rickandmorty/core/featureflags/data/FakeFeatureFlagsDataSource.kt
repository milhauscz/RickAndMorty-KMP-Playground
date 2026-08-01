package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig

class FakeFeatureFlagsDataSource(
    var config: Map<String, RemoteFlagConfig> = emptyMap(),
) : FeatureFlagsDataSource {
    var error: DataError.Remote? = null
    var fetchCount: Int = 0

    override suspend fun fetch(): Result<Map<String, RemoteFlagConfig>, DataError.Remote> {
        fetchCount++
        return error?.let { Result.Error(it) } ?: Result.Success(config)
    }
}
