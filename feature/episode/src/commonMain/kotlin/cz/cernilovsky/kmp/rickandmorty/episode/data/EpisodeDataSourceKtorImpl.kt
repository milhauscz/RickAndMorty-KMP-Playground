package cz.cernilovsky.kmp.rickandmorty.episode.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import cz.cernilovsky.kmp.rickandmorty.core.network.safeCall
import cz.cernilovsky.kmp.rickandmorty.episode.data.remote.EpisodeDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

class EpisodeDataSourceKtorImpl(
    private val httpClient: HttpClient,
) : EpisodeDataSource {
    override suspend fun getEpisodes(ids: List<Int>): Result<List<EpisodeDto>, DataError.Remote> {
        if (ids.isEmpty()) return Result.Success(emptyList())

        val url = "${NetworkConfig.BASE_URL}/episode/${ids.joinToString(",")}"

        return when (val result = safeCall<JsonElement> { httpClient.get(url) }) {
            is Result.Error -> result
            is Result.Success -> result.data.toEpisodeDtos()
        }
    }

    // The API returns a JSON array for multiple ids but a single object for one id.
    private fun JsonElement.toEpisodeDtos(): Result<List<EpisodeDto>, DataError.Remote> =
        try {
            val dtos =
                when (this) {
                    is JsonArray -> map { json.decodeFromJsonElement(EpisodeDto.serializer(), it) }
                    else -> listOf(json.decodeFromJsonElement(EpisodeDto.serializer(), this))
                }
            Result.Success(dtos)
        } catch (_: SerializationException) {
            Result.Error(DataError.Remote.SERIALIZATION)
        }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
