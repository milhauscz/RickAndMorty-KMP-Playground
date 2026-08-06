package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharactersLoadType
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharactersPageLoadResult
import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result

public class LoadCharactersUseCase(
    private val charactersRepository: CharactersRepository,
) {
    public suspend operator fun invoke(
        loadType: CharactersLoadType,
        filters: CharacterFilters,
        anchorCharacterId: Int? = null,
    ): Result<CharactersPageLoadResult, DataError.Remote> =
        charactersRepository.loadCharacters(loadType, filters, anchorCharacterId)
}
