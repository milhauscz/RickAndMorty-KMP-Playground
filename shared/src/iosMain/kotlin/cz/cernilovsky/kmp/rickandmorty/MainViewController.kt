package cz.cernilovsky.kmp.rickandmorty

import androidx.compose.ui.window.ComposeUIViewController
import cz.cernilovsky.kmp.rickandmorty.di.initKoin

@Suppress("FunctionNaming")
fun MainViewController() =
    ComposeUIViewController(
        configure = {
            initKoin()
        },
    ) { App() }
