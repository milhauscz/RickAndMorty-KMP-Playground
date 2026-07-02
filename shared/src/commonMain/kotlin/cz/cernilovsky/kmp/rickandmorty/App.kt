package cz.cernilovsky.kmp.rickandmorty

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.compose.setSingletonImageLoaderFactory
import cz.cernilovsky.kmp.rickandmorty.characters.ui.CharacterListDetailScreen
import cz.cernilovsky.kmp.rickandmorty.characters.ui.CharacterListScreen
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailScreen
import cz.cernilovsky.kmp.rickandmorty.characters.ui.filters.CharacterFiltersScreen
import cz.cernilovsky.kmp.rickandmorty.core.image.createImageLoader
import cz.cernilovsky.kmp.rickandmorty.core.ui.LocalSharedTransitionContext
import cz.cernilovsky.kmp.rickandmorty.core.ui.SharedTransitionContext
import cz.cernilovsky.kmp.rickandmorty.core.ui.theme.RickAndMortyTheme
import cz.cernilovsky.kmp.rickandmorty.navigation.CharacterDetailRoute
import cz.cernilovsky.kmp.rickandmorty.navigation.CharacterFiltersRoute
import cz.cernilovsky.kmp.rickandmorty.navigation.CharacterListRoute

// Material 3 window-size-class "Expanded" width breakpoint: switch to the two-pane layout at/above.
private val EXPANDED_WIDTH_BREAKPOINT = 840.dp

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context -> createImageLoader(context) }
    RickAndMortyTheme {
        val navController = rememberNavController()
        BoxWithConstraints(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
        ) {
            val isExpandedWidth = maxWidth >= EXPANDED_WIDTH_BREAKPOINT
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = CharacterListRoute,
                ) {
                    composable<CharacterListRoute> {
                        if (isExpandedWidth) {
                            // Two-pane list/detail; navigation to the detail route is not used here.
                            CharacterListDetailScreen(
                                onFilterClick = { navController.navigate(CharacterFiltersRoute) },
                            )
                        } else {
                            val context = SharedTransitionContext(
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable
                            )

                            CompositionLocalProvider(LocalSharedTransitionContext provides context) {
                                CharacterListScreen(
                                    onCharacterClick = { id -> navController.navigate(CharacterDetailRoute(id)) },
                                    onFilterClick = { navController.navigate(CharacterFiltersRoute) },
                                )
                            }
                        }
                    }
                    composable<CharacterFiltersRoute>(
                        enterTransition = { slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth / 4 }) },
                        popEnterTransition = { slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth / 4 }) },
                        popExitTransition = { slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) },
                    ) {
                        CharacterFiltersScreen(onBack = { navController.navigateUp() })
                    }
                    composable<CharacterDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<CharacterDetailRoute>()
                        val context = SharedTransitionContext(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                        CompositionLocalProvider(LocalSharedTransitionContext provides context) {
                            CharacterDetailScreen(
                                characterId = route.id,
                                onBack = { navController.navigateUp() },
                            )
                        }
                    }
                }
            }
        }
        StatusBarProtection()
    }
}

@Composable
private fun StatusBarProtection(
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                with(LocalDensity.current) {
                    (WindowInsets.statusBars.getTop(this) * 1.2f).toDp()
                }
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 1f),
                        color.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
    )
}
