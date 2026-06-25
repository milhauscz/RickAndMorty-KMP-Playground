package cz.cernilovsky.android.rickandmorty.characters.data

import cz.cernilovsky.android.rickandmorty.characters.data.mapper.toDomain
import cz.cernilovsky.android.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharactersResponse
import cz.cernilovsky.android.rickandmorty.core.domain.DataError
import cz.cernilovsky.android.rickandmorty.core.domain.Result
import cz.cernilovsky.android.rickandmorty.core.domain.map

class CharactersRepository(private val charactersDataSource: ICharactersDataSource) :
    ICharactersRepository {
    override suspend fun getCharacters(page: Int): Result<CharactersResponse, DataError.Remote> {
        return charactersDataSource.getCharacters(page).map {
            it.toDomain()
        }
    }

}