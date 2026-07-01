package cz.cernilovsky.kmp.rickandmorty.characters.domain.model

import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location

data class CharacterDetail(
    val character: Character,
    val origin: Location?,
    val location: Location?,
    val episodes: List<Episode>,
)
