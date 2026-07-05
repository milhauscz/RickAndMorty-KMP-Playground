package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.window.core.layout.WindowSizeClass
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailScreen
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.IMAGE_HEIGHT
import cz.cernilovsky.kmp.rickandmorty.characters.ui.list.CharacterListActions
import cz.cernilovsky.kmp.rickandmorty.characters.ui.list.CharacterListScreen
import cz.cernilovsky.kmp.rickandmorty.core.ui.LocalSharedTransitionContext
import cz.cernilovsky.kmp.rickandmorty.core.ui.SharedTransitionContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// The list pane takes this fraction of the available width, clamped so it neither cramps a
// barely-expanded window nor grows absurdly wide on a large tablet. Only applies in two-pane mode;
// in single-pane mode the visible pane fills the window and the preferred width is ignored.
private const val LIST_PANE_WIDTH_FRACTION = 0.3f
private val LIST_PANE_MIN_WIDTH = 400.dp
private val LIST_PANE_MAX_WIDTH = 500.dp

// The detail pane's content is capped so its hero image box (fixed height, full pane width)
// doesn't get stretched into an extreme aspect ratio that crops the avatar down to a sliver.
private val DETAIL_PANE_MAX_WIDTH = 700.dp

// On tall windows the detail pane has headroom to show a taller hero image; on short windows
// (e.g. a phone rotated to landscape) that would shrink the image below its default height, so
// the taller image is only used once the window's height reaches Expanded per WindowSizeClass.
private const val DETAIL_IMAGE_HEIGHT_FRACTION = 0.5f

// Crossfade duration for a pane appearing/disappearing. Kept deliberately slow so the single <-> two
// pane switch (e.g. rotating the phone) reads as a fade rather than a fast flicker.
private const val PANE_FADE_DURATION_MILLIS = 1000

/**
 * Adaptive list/detail screen for the whole character browse flow, backed by Material 3's
 * [ListDetailPaneScaffold]. The scaffold owns the single- vs two-pane decision, so `App` mounts this
 * on every window size and no longer folds a separate detail route in and out at the breakpoint:
 *
 * - On Expanded-width windows both panes are shown side by side.
 * - On smaller windows one pane shows at a time; tapping a character navigates the scaffold to the
 *   detail pane and back navigates to the list pane. In that single-pane mode the list -> detail
 *   avatar shared-element transition runs and the detail pane shows a back button.
 *
 * Selection lives in the repository (observed via [CharactersViewModel.selectedCharacterId]); a tap
 * both writes it and drives the scaffold. Because the repository resets the selection to the first
 * character on every refresh, there is no UI-side auto-select to do here.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CharacterListDetailScreen(onFilterClick: () -> Unit) {
    val viewModel = koinViewModel<CharactersViewModel>()
    val characters = viewModel.charactersPagingFlow.collectAsLazyPagingItems()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedCharacterId.collectAsStateWithLifecycle()

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

    // Single-pane (compact/medium width) shows one pane at a time and animates between them; only
    // then do we run the shared-element avatar transition and show a back button. In two-pane
    // (Expanded) both panes are visible at once, so registering the same avatar shared element in
    // both would collide - a null transition context makes registerSharedElement a no-op there.
    val isSinglePane = navigator.scaffoldDirective.maxHorizontalPartitions == 1

    // In two-pane mode each pane touches only one horizontal window edge, so its inner Scaffold must
    // not pad for a display cutout on the *other* edge (which sits behind the neighbouring pane).
    // Consume the safeDrawing inset for the edge this pane does not touch: the list pane never reaches
    // the end edge, the detail pane never reaches the start edge. In single-pane mode the visible pane
    // spans the full width and handles both edges itself, so nothing is consumed.
    val listPaneCutoutConsumption =
        if (isSinglePane) WindowInsets(0) else WindowInsets.safeDrawing.only(WindowInsetsSides.End)
    val detailPaneCutoutConsumption =
        if (isSinglePane) WindowInsets(0) else WindowInsets.safeDrawing.only(WindowInsetsSides.Start)

    // Fold the detail pane back to the list, animating the scaffold along with the predictive-back
    // gesture: seek the panes as the gesture progresses, then commit on completion (or let the
    // scaffold settle back if it is cancelled). Disabled on the list pane (nothing to go back to),
    // so back there falls through to app navigation.
    PredictiveBackHandler(enabled = navigator.canNavigateBack()) { progress ->
        try {
            progress.collect { backEvent -> navigator.seekBack(fraction = backEvent.progress) }
            navigator.navigateBack()
        } catch (cancellation: CancellationException) {
            navigator.seekBack(fraction = 0f)
            throw cancellation
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val listPaneWidth =
            (maxWidth * LIST_PANE_WIDTH_FRACTION)
                .coerceIn(LIST_PANE_MIN_WIDTH, LIST_PANE_MAX_WIDTH)
        // Upper bound is clamped to at least the lower bound so coerceIn never sees max < min (which
        // it would on windows narrower than IMAGE_HEIGHT + 50.dp, e.g. small phones or split-screen).
        val detailImageHeight: Dp =
            (maxHeight * DETAIL_IMAGE_HEIGHT_FRACTION)
                .coerceIn(IMAGE_HEIGHT, (maxWidth - 50.dp).coerceAtLeast(IMAGE_HEIGHT))

        SharedTransitionLayout {
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                scaffoldState = navigator.scaffoldState,
                listPane = {
                    // Plain crossfade instead of the default slide/expand so the pane's own motion
                    // doesn't fight the list -> detail shared-element avatar transition in single-pane mode.
                    AnimatedPane(
                        modifier =
                            Modifier
                                .preferredWidth(listPaneWidth)
                                .consumeWindowInsets(listPaneCutoutConsumption),
                        enterTransition = fadeIn(tween(PANE_FADE_DURATION_MILLIS)),
                        exitTransition = fadeOut(tween(PANE_FADE_DURATION_MILLIS)),
                    ) {
                        val transitionContext =
                            if (isSinglePane) SharedTransitionContext(this@SharedTransitionLayout, this) else null
                        CompositionLocalProvider(LocalSharedTransitionContext provides transitionContext) {
                            CharacterListScreen(
                                characters = characters,
                                filters = filters,
                                actions =
                                    CharacterListActions(
                                        onCharacterClick = { id ->
                                            viewModel.setSelectedCharacterId(id)
                                            scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                        },
                                        onFilterClick = onFilterClick,
                                        onRemoveFilter = viewModel::removeFilter,
                                        onClearFilters = viewModel::clearFilters,
                                    ),
                                selectedId = if (isSinglePane) null else selectedId,
                            )
                        }
                    }
                },
                detailPane = {
                    AnimatedPane(
                        modifier = Modifier.consumeWindowInsets(detailPaneCutoutConsumption),
                        enterTransition = fadeIn(tween(PANE_FADE_DURATION_MILLIS)),
                        exitTransition = fadeOut(tween(PANE_FADE_DURATION_MILLIS)),
                    ) {
                        val transitionContext =
                            if (isSinglePane) SharedTransitionContext(this@SharedTransitionLayout, this) else null
                        CompositionLocalProvider(LocalSharedTransitionContext provides transitionContext) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                val refresh = characters.loadState.refresh
                                // Snapshot into a local so it smart-casts inside the branch below (selectedId is a
                                // delegated property read from the ViewModel flow, which can't be smart-cast).
                                val currentSelectedId = selectedId
                                // Every branch must emit a concrete node: a bare no-op branch creates no slot-table
                                // group, so when the state changes away from the loading branch Compose never disposes
                                // the spinner, leaving it stuck on screen. An empty Spacer is a real node.
                                when {
                                    // No results for the current filters: leave the detail pane empty.
                                    refresh is LoadState.NotLoading && characters.itemCount == 0 -> {
                                        Spacer(Modifier.fillMaxSize())
                                    }

                                    currentSelectedId != null -> {
                                        val detailModifier =
                                            Modifier
                                                .widthIn(
                                                    max = DETAIL_PANE_MAX_WIDTH,
                                                ).fillMaxHeight()
                                        CharacterDetailScreen(
                                            characterId = currentSelectedId,
                                            onBack = { scope.launch { navigator.navigateBack() } },
                                            showBackButton = isSinglePane,
                                            modifier = detailModifier,
                                            imageHeight = detailImageHeight,
                                        )
                                    }

                                    refresh is LoadState.Loading -> {
                                        MaxSizeLoadingIndicator()
                                    }

                                    else -> {
                                        Spacer(Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                    }
                },
            )
        }
    }
}
