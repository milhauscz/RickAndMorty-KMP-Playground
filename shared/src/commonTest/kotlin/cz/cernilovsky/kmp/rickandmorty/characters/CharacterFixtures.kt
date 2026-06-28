package cz.cernilovsky.kmp.rickandmorty.characters

import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharacterDto
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharacterLocationDto

fun characterDto(
    id: Int = 1,
    name: String = "Rick Sanchez",
    status: String = "Alive",
    gender: String = "Male",
): CharacterDto = CharacterDto(
    id = id,
    name = name,
    status = status,
    species = "Human",
    type = "",
    gender = gender,
    origin = CharacterLocationDto(name = "Earth (C-137)", url = "https://origin"),
    location = CharacterLocationDto(name = "Citadel of Ricks", url = "https://location"),
    image = "https://image/$id.jpeg",
    episode = listOf("https://episode/1", "https://episode/2"),
    url = "https://character/$id",
    created = "2017-11-04T18:48:46.250Z",
)
