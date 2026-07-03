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
    private val locationJson =
        """
        {
          "id": 1, "name": "Earth (C-137)", "type": "Planet", "dimension": "Dimension C-137",
          "url": "https://rickandmortyapi.com/api/location/1",
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
    fun `valid response parses the location`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.OK, locationJson).getLocation("https://location/1")

            assertIs<Result.Success<LocationDto>>(result)
            assertEquals("Earth (C-137)", result.data.name)
            assertEquals("Planet", result.data.type)
            assertEquals("Dimension C-137", result.data.dimension)
        }

    @Test
    fun `malformed body returns SERIALIZATION error`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.OK, body = "not json").getLocation("https://location/1")

            assertEquals(Result.Error(DataError.Remote.SERIALIZATION), result)
        }

    @Test
    fun `not found maps to NOT_FOUND`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.NotFound).getLocation("https://location/999")

            assertEquals(Result.Error(DataError.Remote.NOT_FOUND), result)
        }

    @Test
    fun `server error maps to SERVER`() =
        runTest {
            val result = dataSourceReturning(HttpStatusCode.InternalServerError).getLocation("https://location/1")

            assertEquals(Result.Error(DataError.Remote.SERVER), result)
        }
}
