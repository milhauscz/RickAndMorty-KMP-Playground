package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

public class ObserveSelectedCharacterIdUseCase(
    private val charactersRepository: CharactersRepository,
) {
    public operator fun invoke(): Flow<Int?> = charactersRepository.selectedCharacterId
}
