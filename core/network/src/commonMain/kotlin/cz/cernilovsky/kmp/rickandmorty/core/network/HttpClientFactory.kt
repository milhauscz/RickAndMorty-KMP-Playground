package cz.cernilovsky.kmp.rickandmorty.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TIMEOUT_MILLIS = 20_000L

object HttpClientFactory {
    fun create(
        engine: HttpClientEngine,
        isDebug: Boolean,
    ): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    json =
                        Json {
                            ignoreUnknownKeys = true
                        },
                )
            }
            install(HttpTimeout) {
                socketTimeoutMillis = TIMEOUT_MILLIS
                requestTimeoutMillis = TIMEOUT_MILLIS
            }
            install(HttpCache)
            if (isDebug) {
                install(Logging) {
                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                println(message)
                            }
                        }
                    level = LogLevel.ALL
                }
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
}
