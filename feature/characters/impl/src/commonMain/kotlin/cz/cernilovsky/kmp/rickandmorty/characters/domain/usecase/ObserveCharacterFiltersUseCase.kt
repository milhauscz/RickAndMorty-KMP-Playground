package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import kotlinx.coroutines.flow.Flow

public class ObserveCharacterFiltersUseCase(
    private val charactersRepository: CharactersRepository,
) {
    public operator fun invoke(): Flow<CharacterFilters> = charactersRepository.filters
}
