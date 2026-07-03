package cz.cernilovsky.kmp.rickandmorty.episode.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "episodes", indices = [Index("url")])
data class EpisodeEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val airDate: String,
    val episode: String,
    val url: String,
    val created: String,
)
