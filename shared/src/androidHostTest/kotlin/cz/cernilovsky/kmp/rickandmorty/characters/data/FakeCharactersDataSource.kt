package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result

class FakeCharactersDataSource : ICharactersDataSource {
    var result: Result<CharactersResponseDto, DataError.Remote> =
        Result.Error(DataError.Remote.UNKNOWN)

    var lastRequestedUrl: String? = null

    override suspend fun getCharacters(url: String): Result<CharactersResponseDto, DataError.Remote> {
        lastRequestedUrl = url
        return result
    }
}
