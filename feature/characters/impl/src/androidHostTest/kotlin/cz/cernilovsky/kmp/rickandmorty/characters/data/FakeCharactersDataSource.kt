package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result

internal class FakeCharactersDataSource : CharactersDataSource {
    var result: Result<CharactersResponseDto, DataError.Remote> =
        Result.Error(DataError.Remote.UNKNOWN)

    var lastRequestedUrl: String? = null
    var requestCount = 0

    override suspend fun getCharacters(url: String): Result<CharactersResponseDto, DataError.Remote> {
        lastRequestedUrl = url
        requestCount++
        return result
    }
}
