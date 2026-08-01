package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import io.ktor.http.URLBuilder

internal fun buildCharactersUrl(
    filters: CharacterFilters,
    baseUrl: String = NetworkConfig.DEFAULT_BASE_URL,
): String =
    URLBuilder("$baseUrl/character")
        .apply {
            filters.name?.let { parameters.append("name", it) }
            filters.species?.let { parameters.append("species", it) }
            filters.type?.let { parameters.append("type", it) }
            filters.status?.let { parameters.append("status", it.toQueryValue()) }
            filters.gender?.let { parameters.append("gender", it.toQueryValue()) }
        }.buildString()

private fun CharacterStatus.toQueryValue(): String =
    when (this) {
        CharacterStatus.Alive -> "alive"
        CharacterStatus.Dead -> "dead"
        CharacterStatus.Unknown -> "unknown"
    }

private fun CharacterGender.toQueryValue(): String =
    when (this) {
        CharacterGender.Female -> "female"
        CharacterGender.Male -> "male"
        CharacterGender.Genderless -> "genderless"
        CharacterGender.Unknown -> "unknown"
    }
