package cz.cernilovsky.kmp.rickandmorty.characters.domain.model

import cz.cernilovsky.kmp.rickandmorty.core.domain.model.Info

data class CharactersResponse(
    val info: Info,
    val characters: List<Character>,
)
