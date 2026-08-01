package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharactersResponse
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result

public class GetCharacterPageUseCase(
    private val charactersRepository: CharactersRepository,
) {
    public suspend operator fun invoke(
        page: Int,
        filters: CharacterFilters,
    ): Result<CharactersResponse, DataError.Remote> = charactersRepository.fetchCharacterPage(page, filters)
}
