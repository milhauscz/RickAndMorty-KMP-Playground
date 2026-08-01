package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository

public class SetSelectedCharacterIdUseCase(
    private val charactersRepository: CharactersRepository,
) {
    public suspend operator fun invoke(id: Int?): Unit = charactersRepository.setSelectedCharacterId(id)
}
