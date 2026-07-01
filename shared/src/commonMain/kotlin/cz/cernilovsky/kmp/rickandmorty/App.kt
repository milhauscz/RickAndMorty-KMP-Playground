package cz.cernilovsky.kmp.rickandmorty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.compose.setSingletonImageLoaderFactory
import cz.cernilovsky.kmp.rickandmorty.characters.ui.CharacterListScreen
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailScreen
import cz.cernilovsky.kmp.rickandmorty.core.image.createImageLoader
import cz.cernilovsky.kmp.rickandmorty.navigation.CharacterDetailRoute
import cz.cernilovsky.kmp.rickandmorty.navigation.CharacterListRoute

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context -> createImageLoader(context) }
    MaterialTheme {
        val navController = rememberNavController()
        Box(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .safeContentPadding()
                    .fillMaxSize(),
        ) {
            NavHost(
                navController = navController,
                startDestination = CharacterListRoute,
            ) {
                composable<CharacterListRoute> {
                    CharacterListScreen(
                        onCharacterClick = { id -> navController.navigate(CharacterDetailRoute(id)) },
                    )
                }
                composable<CharacterDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<CharacterDetailRoute>()
                    CharacterDetailScreen(
                        characterId = route.id,
                        onBack = { navController.navigateUp() },
                    )
                }
            }
        }
    }
}
