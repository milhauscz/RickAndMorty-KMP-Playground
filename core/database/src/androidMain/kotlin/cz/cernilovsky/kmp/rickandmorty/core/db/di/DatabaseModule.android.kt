package cz.cernilovsky.kmp.rickandmorty.core.db.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room.RoomDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.AppDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.DatabaseConfig
import cz.cernilovsky.kmp.rickandmorty.core.db.getAppDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val databasePlatformModule: Module =
    module {
        single<RoomDatabase.Builder<AppDatabase>> {
            getAppDatabaseBuilder(get())
        }
        single {
            val debuggable = (get<Context>().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            DatabaseConfig(allowDestructiveMigration = debuggable)
        }
    }
