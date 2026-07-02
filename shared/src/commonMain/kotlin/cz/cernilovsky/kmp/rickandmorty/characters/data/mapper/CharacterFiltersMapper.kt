package cz.cernilovsky.kmp.rickandmorty.characters.data.mapper

import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharactersMetadataEntity
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus

fun CharactersMetadataEntity?.toFilters(): CharacterFilters =
    CharacterFilters(
        name = this?.filterName,
        species = this?.filterSpecies,
        type = this?.filterType,
        status = this?.filterStatus?.let { stored -> CharacterStatus.entries.firstOrNull { it.name == stored } },
        gender = this?.filterGender?.let { stored -> CharacterGender.entries.firstOrNull { it.name == stored } },
    )
