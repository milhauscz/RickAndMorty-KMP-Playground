package cz.cernilovsky.kmp.rickandmorty

import androidx.compose.ui.window.ComposeUIViewController
import cz.cernilovsky.kmp.rickandmorty.characters.initializeWidget
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdk

@Suppress("FunctionNaming")
fun MainViewController() =
    ComposeUIViewController(
        configure = {
            RickAndMortySdk.initializeWidget()
        },
    ) { App() }
