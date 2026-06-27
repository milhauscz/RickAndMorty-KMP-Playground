package cz.cernilovsky.kmp.rickandmorty.characters.ui

import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character

fun Character.toUiCharacter() = UiCharacter(
    id = id,
    name = name,
    status = status,
    species = species,
    location = location,
    image = image,
)
