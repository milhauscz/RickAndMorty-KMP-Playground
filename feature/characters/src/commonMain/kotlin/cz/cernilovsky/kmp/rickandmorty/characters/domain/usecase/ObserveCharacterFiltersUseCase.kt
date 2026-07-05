package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import kotlinx.coroutines.flow.Flow

class ObserveCharacterFiltersUseCase(
    private val charactersRepository: ICharactersRepository,
) {
    operator fun invoke(): Flow<CharacterFilters> = charactersRepository.filters
}
