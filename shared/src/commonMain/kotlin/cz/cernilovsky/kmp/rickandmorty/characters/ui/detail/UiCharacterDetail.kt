package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location

data class UiCharacterDetail(
    val id: Int,
    val name: String,
    val image: String,
    val status: CharacterStatus,
    val species: String,
    val gender: CharacterGender,
    val originName: String,
    val origin: Location?,
    val locationName: String,
    val location: Location?,
    val episodes: List<Episode>,
)
