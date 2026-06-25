package cz.cernilovsky.android.rickandmorty

import androidx.compose.ui.window.ComposeUIViewController
import cz.cernilovsky.android.rickandmorty.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }