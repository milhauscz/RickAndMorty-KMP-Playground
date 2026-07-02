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
import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.episode.data.local.EpisodeEntity
import cz.cernilovsky.kmp.rickandmorty.location.data.LocationRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.location.data.local.LocationEntity

@ConstructedBy(AppDatabaseCreator::class)
@TypeConverters(CharacterConverters::class)
@Database(
    entities = [
        CharacterEntity::class,
        CharacterRemoteKeyEntity::class,
        CharactersMetadataEntity::class,
        LocationEntity::class,
        EpisodeEntity::class,
    ],
    version = DB_VERSION,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun charactersDao(): CharactersRoomDataSource

    abstract fun locationDao(): LocationRoomDataSource

    abstract fun episodeDao(): EpisodeRoomDataSource

    companion object {
        const val DB_VERSION = 3
        const val DB_NAME = "app_database"
    }
}
