package cz.cernilovsky.kmp.rickandmorty.location.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientFactory
import cz.cernilovsky.kmp.rickandmorty.location.data.remote.LocationDto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LocationDataSourceTest {
    private fun locationJson(
        id: Int,
        name: String,
    ): String =
        """
        {
          "id": $id, "name": "$name", "type": "Planet", "dimension": "Dimension C-137",
          "url": "https://rickandmortyapi.com/api/location/$id",
          "created": "2017-11-10T12:42:04.162Z"
        }
        """.trimIndent()

    private fun dataSourceReturning(
        status: HttpStatusCode,
        body: String = "",
    ): LocationDataSource {
        val engine =
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        return LocationDataSource(HttpClientFactory.create(engine))
    }

    @Test
    fun `multiple ids parses a json array`() =
        runTest {
            val body = "[ ${locationJson(3, "Citadel of Ricks")}, ${locationJson(21, "Testicle Monster Dimension")} ]"

            val result = dataSourceReturning(HttpStatusCode.OK, body).getLocations(listOf(3, 21))

            assertIs<Result.Success<List<LocationDto>>>(result)
            assertEquals(2, result.data.size)
            assertEquals("Citadel of Ricks", result.data.first().name)
        }

    @Test
    fun `single id parses a bare json object`() =
        runTest {
            val body = locationJson(1, "Earth (C-137)")

            val result = dataSourceReturning(HttpStatusCode.OK, body).getLocations(listOf(1))

            assertIs<Result.Success<List<LocationDto>>>(result)
            assertEquals(1, result.data.size)
            assertEquals("Earth (C-137)", result.data.single().name)
        }

    @Test
    fun `empty ids short-circuits without a network call`() =
        runTest {
            val engine = MockEngine { throw IllegalStateException("should not be called") }
            val dataSource = LocationDataSource(HttpClientFactory.create(engine))

            val result = dataSource.getLocations(emptyList())

            assertEquals(Result.Success(emptyList()), result)
        }

    @Test
    fun `malformed body returns SERIALIZATION error`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.OK, body = "not json").getLocations(listOf(1))

            assertEquals(Result.Error(DataError.Remote.SERIALIZATION), result)
        }

    @Test
    fun `server error maps to SERVER`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.InternalServerError).getLocations(listOf(1))

            assertEquals(Result.Error(DataError.Remote.SERVER), result)
        }
}
