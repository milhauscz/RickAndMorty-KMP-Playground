package cz.cernilovsky.android.rickandmorty.characters.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharacterStatus

@Entity(tableName = "characters")
@TypeConverters(CharacterConverters::class)
data class CharacterEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val status: CharacterStatus,
    val species: String,
    val type: String,
    val gender: CharacterGender,
    @Embedded(prefix = "origin_")
    val origin: CharacterLocationEntity,
    @Embedded(prefix = "location_")
    val location: CharacterLocationEntity,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String,
    val favorite: Boolean = false
)

data class CharacterLocationEntity(
    val name: String,
    val url: String,
)
