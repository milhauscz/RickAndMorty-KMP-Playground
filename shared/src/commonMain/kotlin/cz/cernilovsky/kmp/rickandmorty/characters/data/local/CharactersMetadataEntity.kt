package cz.cernilovsky.kmp.rickandmorty.characters.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters_metadata")
data class CharactersMetadataEntity(
    @PrimaryKey val id: Int = 1,
    val lastUpdated: Long = 0,
    val filterName: String? = null,
    val filterSpecies: String? = null,
    val filterType: String? = null,
    val filterStatus: String? = null,
    val filterGender: String? = null,
    val appliedFiltersKey: String? = null,
)
