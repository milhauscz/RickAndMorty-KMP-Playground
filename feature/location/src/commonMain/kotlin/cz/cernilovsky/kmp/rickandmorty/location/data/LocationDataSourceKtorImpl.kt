package cz.cernilovsky.kmp.rickandmorty.location.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import cz.cernilovsky.kmp.rickandmorty.core.network.safeCall
import cz.cernilovsky.kmp.rickandmorty.location.data.remote.LocationDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

class LocationDataSourceKtorImpl(
    private val httpClient: HttpClient,
) : LocationDataSource {
    override suspend fun getLocations(ids: List<Int>): Result<List<LocationDto>, DataError.Remote> {
        if (ids.isEmpty()) return Result.Success(emptyList())

        val url = "${NetworkConfig.BASE_URL}/location/${ids.joinToString(",")}"

        return when (val result = safeCall<JsonElement> { httpClient.get(url) }) {
            is Result.Error -> result
            is Result.Success -> result.data.toLocationDtos()
        }
    }

    // The API returns a JSON array for multiple ids but a single object for one id.
    private fun JsonElement.toLocationDtos(): Result<List<LocationDto>, DataError.Remote> =
        try {
            val dtos =
                when (this) {
                    is JsonArray -> map { json.decodeFromJsonElement(LocationDto.serializer(), it) }
                    else -> listOf(json.decodeFromJsonElement(LocationDto.serializer(), this))
                }
            Result.Success(dtos)
        } catch (_: SerializationException) {
            Result.Error(DataError.Remote.SERIALIZATION)
        }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
