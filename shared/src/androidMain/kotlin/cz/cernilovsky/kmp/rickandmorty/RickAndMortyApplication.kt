package cz.cernilovsky.kmp.rickandmorty

import android.app.Application
import cz.cernilovsky.kmp.rickandmorty.di.initKoin
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
