package cz.cernilovsky.android.rickandmorty.characters.domain.model

import cz.cernilovsky.android.rickandmorty.core.domain.model.Info

data class CharactersResponse(
    val info: Info,
    val characters: List<Character>,
)
