package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters

class SetCharacterFiltersUseCase(
    private val charactersRepository: ICharactersRepository,
) {
    suspend operator fun invoke(filters: CharacterFilters) = charactersRepository.setFilters(filters)
}
