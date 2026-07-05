package cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase

import androidx.paging.PagingData
import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import kotlinx.coroutines.flow.Flow

class GetCharactersUseCase(
    private val charactersRepository: ICharactersRepository,
) {
    operator fun invoke(): Flow<PagingData<Character>> = charactersRepository.charactersPagingData
}
