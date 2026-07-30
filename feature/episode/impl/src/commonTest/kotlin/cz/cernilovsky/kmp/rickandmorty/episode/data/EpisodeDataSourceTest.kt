package cz.cernilovsky.kmp.rickandmorty.episode.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientFactory
import cz.cernilovsky.kmp.rickandmorty.episode.data.remote.EpisodeDto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EpisodeDataSourceTest {
    private fun episodeJson(
        id: Int,
        name: String,
    ): String =
        """
        {
          "id": $id, "name": "$name", "air_date": "December 2, 2013",
          "episode": "S01E0$id", "url": "https://rickandmortyapi.com/api/episode/$id",
          "created": "2017-11-10T12:56:33.798Z"
        }
        """.trimIndent()

    private fun dataSourceReturning(
        status: HttpStatusCode,
        body: String = "",
    ): EpisodeDataSource {
        val engine =
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        return EpisodeDataSourceKtorImpl(HttpClientFactory.create(engine, isDebug = false))
    }

    @Test
    fun `multiple ids parses a json array`() =
        runTest {
            val body = "[ ${episodeJson(1, "Pilot")}, ${episodeJson(2, "Lawnmower Dog")} ]"

            val result = dataSourceReturning(HttpStatusCode.OK, body).getEpisodes(listOf(1, 2))

            assertIs<Result.Success<List<EpisodeDto>>>(result)
            assertEquals(2, result.data.size)
            assertEquals("Pilot", result.data.first().name)
        }

    @Test
    fun `single id parses a bare json object`() =
        runTest {
            val body = episodeJson(1, "Pilot")

            val result = dataSourceReturning(HttpStatusCode.OK, body).getEpisodes(listOf(1))

            assertIs<Result.Success<List<EpisodeDto>>>(result)
            assertEquals(1, result.data.size)
            assertEquals("Pilot", result.data.single().name)
        }

    @Test
    fun `empty ids short-circuits without a network call`() =
        runTest {
            val engine = MockEngine { throw IllegalStateException("should not be called") }
            val dataSource = EpisodeDataSourceKtorImpl(HttpClientFactory.create(engine, isDebug = false))

            val result = dataSource.getEpisodes(emptyList())

            assertEquals(Result.Success(emptyList()), result)
        }

    @Test
    fun `malformed body returns SERIALIZATION error`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.OK, body = "not json").getEpisodes(listOf(1))

            assertEquals(Result.Error(DataError.Remote.SERIALIZATION), result)
        }

    @Test
    fun `server error maps to SERVER`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.InternalServerError).getEpisodes(listOf(1))

            assertEquals(Result.Error(DataError.Remote.SERVER), result)
        }
}
