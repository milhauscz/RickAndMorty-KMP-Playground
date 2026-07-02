package cz.cernilovsky.kmp.rickandmorty.navigation

import kotlinx.serialization.Serializable

@Serializable
object CharacterListRoute

@Serializable
data class CharacterDetailRoute(
    val id: Int,
)

@Serializable
object CharacterFiltersRoute
