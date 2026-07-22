package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result

class FakeCharactersDataSource : ICharactersDataSource {
    var result: Result<CharactersResponseDto, DataError.Remote> =
        Result.Error(DataError.Remote.UNKNOWN)

    var lastRequestedUrl: String? = null
    var lastForceRefresh: Boolean? = null

    override suspend fun getCharacters(
        url: String,
        forceRefresh: Boolean,
    ): Result<CharactersResponseDto, DataError.Remote> {
        lastRequestedUrl = url
        lastForceRefresh = forceRefresh
        return result
    }
}
