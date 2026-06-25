package cz.cernilovsky.android.rickandmorty

import android.app.Application
import cz.cernilovsky.android.rickandmorty.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class RickAndMortyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@RickAndMortyApplication)
        }
    }
}