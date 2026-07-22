package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CharactersDataSourceTest {
    private val url = "https://rickandmortyapi.com/api/character"

    private val validBody =
        """
        {
          "info": { "count": 1, "pages": 1, "next": null, "prev": null },
          "results": [
            {
              "id": 1, "name": "Rick Sanchez", "status": "Alive", "species": "Human",
              "type": "", "gender": "Male",
              "origin": { "name": "Earth", "url": "https://origin" },
              "location": { "name": "Citadel", "url": "https://location" },
              "image": "https://image/1.jpeg",
              "episode": ["https://episode/1"],
              "url": "https://character/1",
              "created": "2017-11-04T18:48:46.250Z"
            }
          ]
        }
        """.trimIndent()

    private fun dataSourceReturning(
        status: HttpStatusCode,
        body: String = "",
    ): CharactersDataSource {
        val engine =
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        return CharactersDataSource(HttpClientFactory.create(engine, isDebug = false))
    }

    private fun dataSourceThrowing(throwable: Throwable): CharactersDataSource {
        val engine = MockEngine { throw throwable }
        return CharactersDataSource(HttpClientFactory.create(engine, isDebug = false))
    }

    @Test
    fun `2xx with valid body returns parsed success`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.OK, validBody).getCharacters(url)

            assertIs<Result.Success<CharactersResponseDto>>(result)
            val response = result.data
            assertEquals(1, response.info.count)
            assertEquals(1, response.results.single().id)
            assertEquals("Rick Sanchez", response.results.single().name)
        }

    @Test
    fun `2xx with malformed body returns SERIALIZATION error`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.OK, body = "not json").getCharacters(url)

            assertEquals(Result.Error(DataError.Remote.SERIALIZATION), result)
        }

    @Test
    fun `http status codes map to remote errors`() =
        runTest {
            assertEquals(
                Result.Error(DataError.Remote.REQUEST_TIMEOUT),
                dataSourceReturning(HttpStatusCode.RequestTimeout).getCharacters(url),
            )
            assertEquals(
                Result.Error(DataError.Remote.TOO_MANY_REQUESTS),
                dataSourceReturning(HttpStatusCode.TooManyRequests).getCharacters(url),
            )
            assertEquals(
                Result.Error(DataError.Remote.SERVER),
                dataSourceReturning(HttpStatusCode.InternalServerError).getCharacters(url),
            )
            assertEquals(
                Result.Error(DataError.Remote.UNKNOWN),
                dataSourceReturning(HttpStatusCode.BadRequest).getCharacters(url),
            )
            assertEquals(
                Result.Error(DataError.Remote.NOT_FOUND),
                dataSourceReturning(HttpStatusCode.NotFound).getCharacters(url),
            )
        }

    @Test
    fun `unresolved address maps to NO_INTERNET`() =
        runTest {
            val result = dataSourceThrowing(UnresolvedAddressException()).getCharacters(url)

            assertEquals(Result.Error(DataError.Remote.NO_INTERNET), result)
        }
}
