package cz.cernilovsky.kmp.rickandmorty.characters.data.remote

import cz.cernilovsky.kmp.rickandmorty.core.data.model.InfoDto
import kotlinx.serialization.Serializable

@Serializable
data class CharactersResponseDto(
    val info: InfoDto,
    val results: List<CharacterDto>,
)
