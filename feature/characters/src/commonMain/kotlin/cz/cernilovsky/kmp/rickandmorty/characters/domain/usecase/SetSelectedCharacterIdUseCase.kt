package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository

class SetSelectedCharacterIdUseCase(
    private val charactersRepository: ICharactersRepository,
) {
    suspend operator fun invoke(id: Int?) = charactersRepository.setSelectedCharacterId(id)
}
