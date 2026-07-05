package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveSelectedCharacterIdUseCase(
    private val charactersRepository: ICharactersRepository,
) {
    operator fun invoke(): StateFlow<Int?> = charactersRepository.selectedCharacterId
}
