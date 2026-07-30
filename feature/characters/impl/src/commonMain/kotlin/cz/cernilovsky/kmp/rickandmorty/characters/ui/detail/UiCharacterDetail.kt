package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.runtime.Immutable
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus

@Immutable
data class UiCharacterDetail(
    val id: Int,
    val name: String,
    val image: String,
    val status: CharacterStatus,
    val species: String,
    val type: String,
    val gender: CharacterGender,
    val originName: String,
    val origin: UiLocation?,
    val locationName: String,
    val location: UiLocation?,
    val episodes: List<UiEpisode>,
)
