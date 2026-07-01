package cz.cernilovsky.kmp.rickandmorty.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room.RoomDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.AppDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.DatabaseConfig
import cz.cernilovsky.kmp.rickandmorty.core.db.getAppDatabaseBuilder
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.koin.dsl.module

actual val platformModule =
    module {
        single<HttpClientEngine> {
            Android.create()
        }
        single<RoomDatabase.Builder<AppDatabase>> {
            getAppDatabaseBuilder(get())
        }
        single {
            val debuggable = (get<Context>().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            DatabaseConfig(allowDestructiveMigration = debuggable)
        }
    }
