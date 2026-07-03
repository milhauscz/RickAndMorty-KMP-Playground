package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterDetail

fun CharacterDetail.toUi(): UiCharacterDetail =
    UiCharacterDetail(
        id = character.id,
        name = character.name,
        image = character.image,
        status = character.status,
        species = character.species,
        type = character.type,
        gender = character.gender,
        originName = character.origin.name,
        origin = origin,
        locationName = character.location.name,
        location = location,
        episodes = episodes,
    )
