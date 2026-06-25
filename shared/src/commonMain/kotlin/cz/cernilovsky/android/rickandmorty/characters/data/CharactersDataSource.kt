package cz.cernilovsky.android.rickandmorty.characters.data

import cz.cernilovsky.android.rickandmorty.characters.data.model.CharactersResponseDto
import cz.cernilovsky.android.rickandmorty.core.domain.DataError
import cz.cernilovsky.android.rickandmorty.core.domain.Result
import cz.cernilovsky.android.rickandmorty.core.network.NetworkConfig
import cz.cernilovsky.android.rickandmorty.core.network.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class CharactersDataSource(
    private val httpClient: HttpClient
) : ICharactersDataSource {
    override suspend fun getCharacters(page: Int): Result<CharactersResponseDto, DataError.Remote> {
        return safeCall {
            httpClient.get("${NetworkConfig.BASE_URL}/character") {
                parameter("page", page)
            }
        }
    }
}