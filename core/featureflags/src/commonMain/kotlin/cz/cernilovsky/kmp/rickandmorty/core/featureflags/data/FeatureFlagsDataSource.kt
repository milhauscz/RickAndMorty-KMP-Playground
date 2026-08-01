package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.domain.map
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig
import cz.cernilovsky.kmp.rickandmorty.core.network.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get

internal interface FeatureFlagsDataSource {
    suspend fun fetch(): Result<Map<String, RemoteFlagConfig>, DataError.Remote>
}

internal class FeatureFlagsDataSourceKtorImpl(
    private val httpClient: HttpClient,
    private val remoteConfigUrl: String,
) : FeatureFlagsDataSource {
    override suspend fun fetch(): Result<Map<String, RemoteFlagConfig>, DataError.Remote> =
        safeCall<FeatureFlagsDto> { httpClient.get(remoteConfigUrl) }
            .map { dto -> dto.toRemoteConfig() }
}
