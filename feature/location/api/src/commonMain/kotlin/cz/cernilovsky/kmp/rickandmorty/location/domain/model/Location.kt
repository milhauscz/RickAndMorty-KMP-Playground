package cz.cernilovsky.kmp.rickandmorty.location.domain.model

public data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val url: String,
    val created: String,
)
