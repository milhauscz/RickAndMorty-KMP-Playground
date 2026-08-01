package cz.cernilovsky.kmp.rickandmorty

import androidx.compose.ui.window.ComposeUIViewController
import cz.cernilovsky.kmp.rickandmorty.characters.di.charactersUiModule
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdk
import cz.cernilovsky.kmp.rickandmorty.runtime.initialize

@OptIn(InternalRickAndMortyApi::class)
@Suppress("FunctionNaming")
fun MainViewController() =
    ComposeUIViewController(
        configure = {
            RickAndMortySdk.initialize(extraModules = listOf(charactersUiModule))
        },
    ) { App() }
