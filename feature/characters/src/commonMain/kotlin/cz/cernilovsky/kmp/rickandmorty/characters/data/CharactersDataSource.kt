package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.network.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class CharactersDataSource(
    private val httpClient: HttpClient,
) : ICharactersDataSource {
    override suspend fun getCharacters(
        url: String,
        forceRefresh: Boolean,
    ): Result<CharactersResponseDto, DataError.Remote> =
        safeCall {
            httpClient.get(url) {
                if (forceRefresh) {
                    header(HttpHeaders.CacheControl, "no-cache")
                }
            }
        }
}
