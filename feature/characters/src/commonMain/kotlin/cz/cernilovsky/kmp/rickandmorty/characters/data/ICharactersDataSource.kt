package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result

interface ICharactersDataSource {
    /**
     * @param forceRefresh bypasses the HTTP client's response cache - used for refreshes the user (or
     * the mediator's own staleness check) explicitly asked for, where a stale cached response would
     * silently defeat the point of asking.
     */
    suspend fun getCharacters(
        url: String,
        forceRefresh: Boolean = false,
    ): Result<CharactersResponseDto, DataError.Remote>
}
