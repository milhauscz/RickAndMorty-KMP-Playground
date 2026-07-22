package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository

class SetSelectedCharacterIdUseCase(
    private val charactersRepository: CharactersRepository,
) {
    suspend operator fun invoke(id: Int?) = charactersRepository.setSelectedCharacterId(id)
}
