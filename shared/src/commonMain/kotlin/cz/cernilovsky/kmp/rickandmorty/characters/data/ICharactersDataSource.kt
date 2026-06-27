package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError

interface ICharactersDataSource {
    suspend fun getCharacters(page: Int): Result<CharactersResponseDto, DataError.Remote>
}