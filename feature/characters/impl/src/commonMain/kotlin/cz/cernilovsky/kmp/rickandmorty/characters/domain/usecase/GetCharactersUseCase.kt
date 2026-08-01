package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import androidx.paging.PagingData
import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import kotlinx.coroutines.flow.Flow

public class GetCharactersUseCase(
    private val charactersRepository: CharactersRepository,
) {
    public operator fun invoke(): Flow<PagingData<Character>> = charactersRepository.charactersPagingData
}
