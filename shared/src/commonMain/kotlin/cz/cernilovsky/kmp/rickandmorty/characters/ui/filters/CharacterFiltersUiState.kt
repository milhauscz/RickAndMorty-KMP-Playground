package cz.cernilovsky.kmp.rickandmorty.characters.ui.filters

import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus

data class CharacterFiltersUiState(
    val name: String = "",
    val species: String = "",
    val type: String = "",
    val status: CharacterStatus? = null,
    val gender: CharacterGender? = null,
    val isApplied: Boolean = false,
) {
    fun toFilters(): CharacterFilters =
        CharacterFilters(
            name = name.trim().ifBlank { null },
            species = species.trim().ifBlank { null },
            type = type.trim().ifBlank { null },
            status = status,
            gender = gender,
        )
}

internal fun CharacterFilters.toUiState(): CharacterFiltersUiState =
    CharacterFiltersUiState(
        name = name.orEmpty(),
        species = species.orEmpty(),
        type = type.orEmpty(),
        status = status,
        gender = gender,
    )
