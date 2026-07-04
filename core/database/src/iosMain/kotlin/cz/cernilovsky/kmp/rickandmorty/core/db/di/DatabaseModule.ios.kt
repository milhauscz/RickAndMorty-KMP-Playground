package cz.cernilovsky.kmp.rickandmorty.core.db.di

import androidx.room.RoomDatabase
import cz.cernilovsky.kmp.rickandmorty.core.BuildConfig
import cz.cernilovsky.kmp.rickandmorty.core.db.AppDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.DatabaseConfig
import cz.cernilovsky.kmp.rickandmorty.core.db.getAppDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val databasePlatformModule: Module =
    module {
        single<RoomDatabase.Builder<AppDatabase>> {
            getAppDatabaseBuilder()
        }
        single {
            DatabaseConfig(allowDestructiveMigration = get<BuildConfig>().isDebug)
        }
    }
