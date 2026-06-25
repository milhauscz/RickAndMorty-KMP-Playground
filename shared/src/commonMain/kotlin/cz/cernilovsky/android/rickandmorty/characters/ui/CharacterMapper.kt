package cz.cernilovsky.android.rickandmorty.characters.ui

import cz.cernilovsky.android.rickandmorty.characters.domain.model.Character

fun Character.toUiCharacter(favorite: Boolean = false) = UiCharacter(
    id = id,
    name = name,
    status = status,
    species = species,
    type = type,
    gender = gender,
    origin = origin,
    location = location,
    image = image,
    episode = episode,
    url = url,
    created = created,
    favorite = favorite,
)
