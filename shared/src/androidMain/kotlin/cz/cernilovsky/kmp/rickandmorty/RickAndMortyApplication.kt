package cz.cernilovsky.kmp.rickandmorty

import android.app.Application
import cz.cernilovsky.kmp.rickandmorty.characters.initializeWidget
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdk

class RickAndMortyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RickAndMortySdk.initializeWidget(context = this)
    }
}
