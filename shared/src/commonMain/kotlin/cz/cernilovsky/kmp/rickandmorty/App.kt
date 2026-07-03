package cz.cernilovsky.kmp.rickandmorty

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowSizeClass
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
            val windowSizeClass = WindowSizeClass(maxWidth.value, maxHeight.value)
            val isExpandedWidth =
                windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

            // Tracks the character shown/selected in the two-pane detail view, so it can be
            // carried across the single-pane <-> two-pane transition in either direction.
            var selectedCharacterId by rememberSaveable { mutableStateOf<Int?>(null) }

            // One-shot request to scroll the single-pane list to a character. Set only when the
            // two-pane -> single-pane transition auto-opens the detail screen (the single-pane
            // list has never shown that selection), and cleared after the scroll so ordinary
            // detail visits keep the list position Navigation restores for them.
            var pendingListScrollId by rememberSaveable { mutableStateOf<Int?>(null) }

            // Fires only when isExpandedWidth actually flips, not on every back-stack change -
            // otherwise a normal back-press from detail to list while staying compact would
            // immediately be pushed forward again by the shrink branch below.
            LaunchedEffect(isExpandedWidth) {
                val entry = navController.currentBackStackEntry
                if (isExpandedWidth) {
                    // Grew into expanded width while viewing a single-pane detail screen: fold it
                    // back into the two-pane layout, keeping that character selected.
                    if (entry?.destination?.hasRoute<CharacterDetailRoute>() == true) {
                        selectedCharacterId = entry.toRoute<CharacterDetailRoute>().id
                        // Pop back to the existing CharacterListRoute entry rather than
                        // navigating to a new one, so its ViewModelStore (and with it the
                        // paging cache the selected character was loaded from) survives - a
                        // fresh instance would restart pagination from the first page and the
                        // scroll-to-selection in CharacterListDetailScreen might never find it.
                        navController.popBackStack(route = CharacterListRoute, inclusive = false)
                    }
                } else {
                    // Shrank below expanded width while in the two-pane layout: open the last
                    // selected character as a single-pane detail screen, and ask the list to
                    // scroll to it once the user navigates back.
                    if (entry?.destination?.hasRoute<CharacterListRoute>() == true) {
                        selectedCharacterId?.let { id ->
                            pendingListScrollId = id
                            navController.navigate(CharacterDetailRoute(id))
                        }
                    }
                }
            }

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
                                selectedId = selectedCharacterId,
                                onSelectedIdChange = { selectedCharacterId = it },
                            )
                        } else {
                            val context =
                                SharedTransitionContext(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@composable,
                                )

                            CompositionLocalProvider(LocalSharedTransitionContext provides context) {
                                CharacterListScreen(
                                    onCharacterClick = { id -> navController.navigate(CharacterDetailRoute(id)) },
                                    onFilterClick = { navController.navigate(CharacterFiltersRoute) },
                                    scrollToId = pendingListScrollId,
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
                        val context =
                            SharedTransitionContext(
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable,
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
