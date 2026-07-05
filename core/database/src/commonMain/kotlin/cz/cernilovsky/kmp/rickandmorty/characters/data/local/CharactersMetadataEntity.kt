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
    val filterStatus: CharacterStatusEntity? = null,
    val filterGender: CharacterGenderEntity? = null,
    val appliedFiltersKey: String? = null,
    // The character selected in the two-pane layout. Persisted so it survives process death; the
    // refresh transaction clears it since a selection can't outlive the list it was made against.
    val selectedCharacterId: Int? = null,
)
