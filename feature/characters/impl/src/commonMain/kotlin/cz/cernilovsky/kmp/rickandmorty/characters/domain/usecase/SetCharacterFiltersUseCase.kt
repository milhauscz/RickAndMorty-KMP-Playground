package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters

public class SetCharacterFiltersUseCase(
    private val charactersRepository: CharactersRepository,
) {
    public suspend operator fun invoke(filters: CharacterFilters): Unit = charactersRepository.setFilters(filters)
}
