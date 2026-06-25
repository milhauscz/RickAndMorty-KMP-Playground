package cz.cernilovsky.android.rickandmorty.characters.domain.usecase

import cz.cernilovsky.android.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharactersResponse
import cz.cernilovsky.android.rickandmorty.core.domain.DataError
import cz.cernilovsky.android.rickandmorty.core.domain.Result

class GetCharactersUseCase(private val charactersRepository: ICharactersRepository) {
    suspend operator fun invoke(page: Int): Result<CharactersResponse, DataError.Remote> {
        return charactersRepository.getCharacters(page)
    }
}