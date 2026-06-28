package cz.cernilovsky.kmp.rickandmorty.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterConverters
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharacterRemoteKeyEntity
import cz.cernilovsky.kmp.rickandmorty.characters.data.local.CharactersMetadataEntity
import cz.cernilovsky.kmp.rickandmorty.core.db.AppDatabase.Companion.DB_VERSION

@ConstructedBy(AppDatabaseCreator::class)
@TypeConverters(CharacterConverters::class)
@Database(
    entities = [CharacterEntity::class, CharacterRemoteKeyEntity::class, CharactersMetadataEntity::class],
    version = DB_VERSION,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun charactersDao(): CharactersRoomDataSource

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "app_database"
    }
}
