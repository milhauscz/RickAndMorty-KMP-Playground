package cz.cernilovsky.kmp.rickandmorty.di

import androidx.room.RoomDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.AppDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.DatabaseConfig
import cz.cernilovsky.kmp.rickandmorty.core.db.getAppDatabaseBuilder
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
actual val platformModule: Module =
    module {
        single<HttpClientEngine> {
            Darwin.create()
        }
        single<RoomDatabase.Builder<AppDatabase>> {
            getAppDatabaseBuilder()
        }
        single {
            DatabaseConfig(allowDestructiveMigration = Platform.isDebugBinary)
        }
    }
