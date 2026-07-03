package cz.cernilovsky.kmp.rickandmorty.characters.data.mapper

import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterLocationEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharacterDto
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterLocation

fun CharacterDto.toEntity(): CharacterEntity = toDomain().toEntity()

fun Character.toEntity(): CharacterEntity =
    CharacterEntity(
        id = id,
        name = name,
        status = status.toEntity(),
        species = species,
        type = type,
        gender = gender.toEntity(),
        origin = origin.toEntity(),
        location = location.toEntity(),
        image = image,
        episode = episode,
        url = url,
        created = created,
    )

fun CharacterEntity.toDomain(): Character =
    Character(
        id = id,
        name = name,
        status = status.toDomain(),
        species = species,
        type = type,
        gender = gender.toDomain(),
        origin = origin.toDomain(),
        location = location.toDomain(),
        image = image,
        episode = episode,
        url = url,
        created = created,
    )

private fun CharacterLocation.toEntity(): CharacterLocationEntity =
    CharacterLocationEntity(
        name = name,
        url = url,
    )

private fun CharacterLocationEntity.toDomain(): CharacterLocation =
    CharacterLocation(
        name = name,
        url = url,
    )
