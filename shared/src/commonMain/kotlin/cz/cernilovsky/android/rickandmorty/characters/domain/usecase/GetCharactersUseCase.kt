package cz.cernilovsky.android.rickandmorty.characters.domain.usecase

import androidx.paging.PagingData
import cz.cernilovsky.android.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.android.rickandmorty.characters.domain.model.Character
import kotlinx.coroutines.flow.Flow

class GetCharactersUseCase(private val charactersRepository: ICharactersRepository) {
    operator fun invoke(): Flow<PagingData<Character>> =
        charactersRepository.getCharactersPagingData()
}
