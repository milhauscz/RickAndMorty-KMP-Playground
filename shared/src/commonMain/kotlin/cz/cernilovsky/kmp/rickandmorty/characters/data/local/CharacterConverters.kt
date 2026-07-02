package cz.cernilovsky.kmp.rickandmorty.characters.data.local

import androidx.room.TypeConverter
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import kotlinx.serialization.json.Json

class CharacterConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromCharacterStatus(value: CharacterStatus): String = value.name

    @TypeConverter
    fun toCharacterStatus(value: String): CharacterStatus = CharacterStatus.valueOf(value)

    @TypeConverter
    fun fromCharacterGender(value: CharacterGender): String = value.name

    @TypeConverter
    fun toCharacterGender(value: String): CharacterGender = CharacterGender.valueOf(value)

    @TypeConverter
    fun fromCharacterStatusEntity(value: CharacterStatusEntity): String = value.name

    @TypeConverter
    fun toCharacterStatusEntity(value: String): CharacterStatusEntity = CharacterStatusEntity.valueOf(value)

    @TypeConverter
    fun fromCharacterGenderEntity(value: CharacterGenderEntity): String = value.name

    @TypeConverter
    fun toCharacterGenderEntity(value: String): CharacterGenderEntity = CharacterGenderEntity.valueOf(value)
}
