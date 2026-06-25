package cz.cernilovsky.android.rickandmorty.characters.data

import cz.cernilovsky.android.rickandmorty.characters.data.model.CharactersResponseDto
import cz.cernilovsky.android.rickandmorty.core.domain.DataError
import cz.cernilovsky.android.rickandmorty.core.domain.Result

interface ICharactersDataSource {
    suspend fun getCharacters(page: Int): Result<CharactersResponseDto, DataError.Remote>
}