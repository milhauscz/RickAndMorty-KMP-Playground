package cz.cernilovsky.kmp.rickandmorty

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.setSingletonImageLoaderFactory
import cz.cernilovsky.kmp.rickandmorty.characters.ui.CharacterListDetailScreen
import cz.cernilovsky.kmp.rickandmorty.characters.ui.filters.CharacterFiltersScreen
import cz.cernilovsky.kmp.rickandmorty.core.image.createImageLoader
import cz.cernilovsky.kmp.rickandmorty.core.ui.theme.RickAndMortyTheme
import cz.cernilovsky.kmp.rickandmorty.navigation.CharacterFiltersRoute
import cz.cernilovsky.kmp.rickandmorty.navigation.CharacterListRoute

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context -> createImageLoader(context) }
    RickAndMortyTheme {
        val navController = rememberNavController()
        Box(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
        ) {
            // The character list/detail flow is a single adaptive destination: CharacterListDetailScreen
            // hosts a ListDetailPaneScaffold that decides single- vs two-pane itself and owns the
            // list <-> detail navigation and its shared-element transition. So there is no separate
            // detail route and no manual breakpoint folding here - only filters is its own route.
            NavHost(
                navController = navController,
                startDestination = CharacterListRoute,
            ) {
                composable<CharacterListRoute> {
                    CharacterListDetailScreen(
                        onFilterClick = { navController.navigate(CharacterFiltersRoute) },
                    )
                }
                composable<CharacterFiltersRoute>(
                    enterTransition = { slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth / 4 }) },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth / 4 }) },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) },
                ) {
                    CharacterFiltersScreen(onBack = { navController.navigateUp() })
                }
            }
        }
        StatusBarProtection()
    }
}

@Composable
private fun StatusBarProtection(color: Color = MaterialTheme.colorScheme.surfaceContainer) {
    Spacer(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    with(LocalDensity.current) {
                        (WindowInsets.statusBars.getTop(this) * 1.2f).toDp()
                    },
                ).background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    color.copy(alpha = 1f),
                                    color.copy(alpha = 0.8f),
                                    Color.Transparent,
                                ),
                        ),
                ),
    )
}
