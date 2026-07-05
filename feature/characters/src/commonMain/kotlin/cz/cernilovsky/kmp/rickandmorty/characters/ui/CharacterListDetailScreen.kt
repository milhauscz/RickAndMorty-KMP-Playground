package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.window.core.layout.WindowSizeClass
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailScreen
import cz.cernilovsky.kmp.rickandmorty.characters.ui.list.CharacterListActions
import cz.cernilovsky.kmp.rickandmorty.characters.ui.list.CharacterListScreen
import cz.cernilovsky.kmp.rickandmorty.characters.ui.list.CharactersViewModel
import org.koin.compose.viewmodel.koinViewModel

// The list pane takes this fraction of the available width, clamped so it neither cramps a
// barely-expanded window nor grows absurdly wide on a large tablet.
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

/**
 * Two-pane list/detail layout used on Expanded-width windows. The character list is shown in the
 * list pane and the selected character's detail in the detail pane, laid out by Material 3's
 * [ListDetailPaneScaffold]. `App` only mounts this on Expanded-width windows, so the scaffold's
 * own adaptive directive keeps both panes visible; the single-pane fallback (with its shared-element
 * transition) lives on a separate navigation route in `App`.
 *
 * The selection lives in the repository (observed via [CharactersViewModel.selectedCharacterId]),
 * so it survives this composable being disposed and remounted - e.g. navigating to the filters
 * screen and back or crossing the pane-layout breakpoint - and is a single source of truth shared
 * with the cross-pane navigation in `App`. The data layer resets it to the first character on every
 * refresh, so the "select first once loaded" behaviour needs no UI-side effect here. A tap just
 * writes the new selection; the scaffold keeps the detail pane on screen, so there is no scaffold
 * navigation to drive.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CharacterListDetailScreen(onFilterClick: () -> Unit) {
    val viewModel = koinViewModel<CharactersViewModel>()
    val characters = viewModel.charactersPagingFlow.collectAsLazyPagingItems()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    // Selection is owned by the data layer: the repository resets it to the first character on every
    // refresh, so there is nothing for the UI to auto-select. A tap just writes the new selection.
    val selectedId by viewModel.selectedCharacterId.collectAsStateWithLifecycle()

    // The navigator sources the scaffold's directive and value from the current window; on the
    // Expanded windows this screen is mounted on, that keeps both panes side by side. Selection stays
    // in the repository, so we never call navigator.navigateTo - there is no scaffold navigation to
    // drive and hence no scaffold back stack. (NavigableListDetailPaneScaffold, which would add
    // predictive-back handling, is Android-only in the Compose Multiplatform adaptive libraries, so
    // the plain multiplatform ListDetailPaneScaffold is used here.)
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val listPaneWidth =
            (maxWidth * LIST_PANE_WIDTH_FRACTION)
                .coerceIn(LIST_PANE_MIN_WIDTH, LIST_PANE_MAX_WIDTH)
        val isMediumHeight = maxHeight.value >= WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
        val detailImageHeight: Dp? = if (isMediumHeight) maxHeight * DETAIL_IMAGE_HEIGHT_FRACTION else null

        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane(modifier = Modifier.preferredWidth(listPaneWidth)) {
                    CharacterListScreen(
                        characters = characters,
                        filters = filters,
                        actions =
                            CharacterListActions(
                                onCharacterClick = viewModel::setSelectedCharacterId,
                                onFilterClick = onFilterClick,
                                onRemoveFilter = viewModel::removeFilter,
                                onClearFilters = viewModel::clearFilters,
                            ),
                        // Scroll once to the character that was already selected when this layout appeared
                        // (carried over from the single-pane detail screen, or restored after returning from
                        // filters). Later taps must not scroll - the user can already see the item they tap -
                        // so the target is captured at mount from the current selection instead of following it.
                        scrollToId = remember { viewModel.selectedCharacterId.value },
                        selectedId = selectedId,
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        val refresh = characters.loadState.refresh
                        // Snapshot into a local so it smart-casts inside the branch below (selectedId is a
                        // delegated property read from the ViewModel flow, which can't be smart-cast).
                        val currentSelectedId = selectedId
                        // Every branch must emit a concrete node: a bare `Unit`/no-op branch creates no
                        // slot-table group, so when the state changes away from the loading branch Compose
                        // never disposes the spinner, leaving it stuck on screen (e.g. after a refresh
                        // fails). An empty Spacer is a real node that correctly replaces it.
                        when {
                            // No results for the current filters: leave the detail pane empty.
                            refresh is LoadState.NotLoading && characters.itemCount == 0 -> {
                                Spacer(Modifier.fillMaxSize())
                            }

                            currentSelectedId != null -> {
                                val detailModifier = Modifier.widthIn(max = DETAIL_PANE_MAX_WIDTH).fillMaxHeight()
                                if (detailImageHeight != null) {
                                    CharacterDetailScreen(
                                        characterId = currentSelectedId,
                                        onBack = {},
                                        showBackButton = false,
                                        modifier = detailModifier,
                                        imageHeight = detailImageHeight,
                                    )
                                } else {
                                    CharacterDetailScreen(
                                        characterId = currentSelectedId,
                                        onBack = {},
                                        showBackButton = false,
                                        modifier = detailModifier,
                                    )
                                }
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
            },
        )
    }
}
