package cz.cernilovsky.kmp.rickandmorty

import android.app.Application
import cz.cernilovsky.kmp.rickandmorty.characters.di.charactersUiModule
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdk
import cz.cernilovsky.kmp.rickandmorty.runtime.initialize

@OptIn(InternalRickAndMortyApi::class)
class RickAndMortyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RickAndMortySdk.initialize(
            context = this,
            extraModules = listOf(charactersUiModule),
        )
    }
}
