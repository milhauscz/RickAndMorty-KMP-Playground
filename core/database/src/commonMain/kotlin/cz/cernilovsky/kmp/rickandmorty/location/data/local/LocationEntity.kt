package cz.cernilovsky.kmp.rickandmorty.location.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "locations", indices = [Index("url")])
data class LocationEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val url: String,
    val created: String,
)
