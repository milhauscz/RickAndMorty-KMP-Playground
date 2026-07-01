package cz.cernilovsky.kmp.rickandmorty.location.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.safeCall
import cz.cernilovsky.kmp.rickandmorty.location.data.remote.LocationDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class LocationDataSource(
    private val httpClient: HttpClient,
) : ILocationDataSource {
    override suspend fun getLocation(url: String): Result<LocationDto, DataError.Remote> =
        safeCall {
            httpClient.get(url)
        }
}
