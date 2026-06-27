package cz.cernilovsky.android.rickandmorty.characters.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters_metadata")
data class CharactersMetadataEntity(
    @PrimaryKey val id: Int = 1,
    val lastUpdated: Long = 0,
)