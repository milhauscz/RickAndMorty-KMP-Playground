package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ObserveSelectedCharacterIdUseCase(
    private val charactersRepository: CharactersRepository,
) {
    operator fun invoke(): Flow<Int?> = charactersRepository.selectedCharacterId
}
