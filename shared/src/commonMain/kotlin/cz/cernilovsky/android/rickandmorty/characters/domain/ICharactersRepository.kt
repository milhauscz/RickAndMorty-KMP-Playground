package cz.cernilovsky.android.rickandmorty.characters.domain

import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharactersResponse
import cz.cernilovsky.android.rickandmorty.core.domain.DataError
import cz.cernilovsky.android.rickandmorty.core.domain.Result

interface ICharactersRepository {
    suspend fun getCharacters(page: Int): Result<CharactersResponse, DataError.Remote>
}