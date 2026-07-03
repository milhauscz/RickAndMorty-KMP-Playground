package cz.cernilovsky.kmp.rickandmorty.core.db.di

import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.core.db.AppDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.DatabaseConfig
import cz.cernilovsky.kmp.rickandmorty.core.db.getAppDatabase
import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.location.data.LocationRoomDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

/** Platform-specific Room builder + database config. */
expect val databasePlatformModule: Module

val databaseModule =
    module {
        single<AppDatabase> {
            getAppDatabase(get(), get<DatabaseConfig>().allowDestructiveMigration)
        }
        single<CharactersRoomDataSource> { get<AppDatabase>().charactersDao() }
        single<LocationRoomDataSource> { get<AppDatabase>().locationDao() }
        single<EpisodeRoomDataSource> { get<AppDatabase>().episodeDao() }
    }
