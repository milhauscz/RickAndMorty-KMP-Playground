package cz.cernilovsky.kmp.rickandmorty.episode.domain.model

data class Episode(
    val id: Int,
    val name: String,
    val airDate: String,
    val episode: String,
    val url: String,
    val created: String,
)
