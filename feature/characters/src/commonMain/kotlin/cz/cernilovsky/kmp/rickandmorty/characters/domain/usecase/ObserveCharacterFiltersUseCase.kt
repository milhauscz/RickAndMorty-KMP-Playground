package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import kotlinx.coroutines.flow.Flow

class ObserveCharacterFiltersUseCase(
    private val charactersRepository: CharactersRepository,
) {
    operator fun invoke(): Flow<CharacterFilters> = charactersRepository.filters
}
