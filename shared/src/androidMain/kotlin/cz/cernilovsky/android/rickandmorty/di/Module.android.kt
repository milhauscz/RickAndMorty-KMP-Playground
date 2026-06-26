package cz.cernilovsky.android.rickandmorty.di

import androidx.room.RoomDatabase
import cz.cernilovsky.android.rickandmorty.core.db.AppDatabase
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import cz.cernilovsky.android.rickandmorty.core.db.getAppDatabaseBuilder
import org.koin.dsl.bind
import org.koin.dsl.binds

actual val platformModule = module {
    single<HttpClientEngine> {
        Android.create()
    }
    single<RoomDatabase.Builder<AppDatabase>> {
        getAppDatabaseBuilder(get())
    }
}