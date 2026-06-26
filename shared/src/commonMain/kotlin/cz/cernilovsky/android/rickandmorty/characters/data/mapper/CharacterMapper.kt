package cz.cernilovsky.android.rickandmorty.characters.data.mapper

import cz.cernilovsky.android.rickandmorty.characters.data.remote.CharacterDto
import cz.cernilovsky.android.rickandmorty.characters.data.remote.CharacterLocationDto
import cz.cernilovsky.android.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.android.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharacterLocation
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharactersResponse
import cz.cernilovsky.android.rickandmorty.core.data.model.InfoDto
import cz.cernilovsky.android.rickandmorty.core.domain.model.Info

fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    status = status.toCharacterStatus(),
    species = species,
    type = type,
    gender = gender.toCharacterGender(),
    origin = origin.toDomain(),
    location = location.toDomain(),
    image = image,
    episode = episode,
    url = url,
    created = created,
)

private fun CharacterLocationDto.toDomain(): CharacterLocation = CharacterLocation(
    name = name,
    url = url,
)

private fun String.toCharacterStatus(): CharacterStatus = when (this) {
    "Alive" -> CharacterStatus.Alive
    "Dead" -> CharacterStatus.Dead
    else -> CharacterStatus.Unknown
}

private fun String.toCharacterGender(): CharacterGender = when (this) {
    "Female" -> CharacterGender.Female
    "Male" -> CharacterGender.Male
    "Genderless" -> CharacterGender.Genderless
    else -> CharacterGender.Unknown
}

fun CharactersResponseDto.toDomain(): CharactersResponse = CharactersResponse(
    info = info.toDomain(),
    characters = results.map { it.toDomain() },
)

private fun InfoDto.toDomain(): Info = Info(
    count = count,
    pages = pages,
    next = next,
    prev = prev,
)
