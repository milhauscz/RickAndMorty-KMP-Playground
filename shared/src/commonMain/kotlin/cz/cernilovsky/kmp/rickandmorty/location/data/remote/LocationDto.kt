package cz.cernilovsky.kmp.rickandmorty.location.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val url: String,
    val created: String,
)
