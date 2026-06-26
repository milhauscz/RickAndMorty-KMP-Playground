package cz.cernilovsky.android.rickandmorty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.setSingletonImageLoaderFactory
import cz.cernilovsky.android.rickandmorty.characters.ui.CharacterListScreen
import cz.cernilovsky.android.rickandmorty.core.image.createImageLoader

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context -> createImageLoader(context) }
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CharacterListScreen()
        }
    }
}