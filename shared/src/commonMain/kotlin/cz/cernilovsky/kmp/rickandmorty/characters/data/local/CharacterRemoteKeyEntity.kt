package cz.cernilovsky.kmp.rickandmorty.characters.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_remote_keys")
data class CharacterRemoteKeyEntity(
    @PrimaryKey
    val characterId: Int,
    val prevKey: Int?,
    val nextKey: Int?,
)
