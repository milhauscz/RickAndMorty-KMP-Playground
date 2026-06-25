package cz.cernilovsky.android.rickandmorty.characters.data.model

import cz.cernilovsky.android.rickandmorty.core.data.model.InfoDto
import kotlinx.serialization.Serializable

@Serializable
data class CharactersResponseDto(
    val info: InfoDto,
    val results: List<CharacterDto>,
)
