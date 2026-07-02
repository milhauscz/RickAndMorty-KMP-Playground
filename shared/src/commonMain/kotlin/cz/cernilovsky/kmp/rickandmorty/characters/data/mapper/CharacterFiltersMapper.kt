package cz.cernilovsky.kmp.rickandmorty.characters.data.mapper

import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterGenderEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterStatusEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharactersMetadataEntity
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus

fun CharactersMetadataEntity?.toFilters(): CharacterFilters =
    CharacterFilters(
        name = this?.filterName,
        species = this?.filterSpecies,
        type = this?.filterType,
        status = this?.filterStatus?.toDomain(),
        gender = this?.filterGender?.toDomain(),
    )

fun CharacterStatusEntity.toDomain(): CharacterStatus =
    when (this) {
        CharacterStatusEntity.Alive -> CharacterStatus.Alive
        CharacterStatusEntity.Dead -> CharacterStatus.Dead
        CharacterStatusEntity.Unknown -> CharacterStatus.Unknown
    }

fun CharacterStatus.toEntity(): CharacterStatusEntity =
    when (this) {
        CharacterStatus.Alive -> CharacterStatusEntity.Alive
        CharacterStatus.Dead -> CharacterStatusEntity.Dead
        CharacterStatus.Unknown -> CharacterStatusEntity.Unknown
    }

fun CharacterGenderEntity.toDomain(): CharacterGender =
    when (this) {
        CharacterGenderEntity.Female -> CharacterGender.Female
        CharacterGenderEntity.Male -> CharacterGender.Male
        CharacterGenderEntity.Genderless -> CharacterGender.Genderless
        CharacterGenderEntity.Unknown -> CharacterGender.Unknown
    }

fun CharacterGender.toEntity(): CharacterGenderEntity =
    when (this) {
        CharacterGender.Female -> CharacterGenderEntity.Female
        CharacterGender.Male -> CharacterGenderEntity.Male
        CharacterGender.Genderless -> CharacterGenderEntity.Genderless
        CharacterGender.Unknown -> CharacterGenderEntity.Unknown
    }
