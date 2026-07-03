package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.window.core.layout.WindowSizeClass
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailScreen
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
 * start pane and the selected character's detail in the end pane. The first character is selected
 * automatically once the list has loaded.
 *
 * Selection state is fully hoisted: this screen holds no copy of its own, so the selection can't
 * get out of sync with (or lost from) the caller when this composable is disposed and remounted,
 * e.g. when navigating to the filters screen and back or crossing the pane-layout breakpoint.
 *
 * @param selectedId Currently selected character, or null when nothing is selected yet.
 * @param onSelectedIdChange Called when the selection should change - from a tap in the list or
 * from the initial auto-selection.
 */
@Composable
fun CharacterListDetailScreen(
    onFilterClick: () -> Unit,
    selectedId: Int?,
    onSelectedIdChange: (Int) -> Unit,
) {
    val viewModel = koinViewModel<CharactersViewModel>()
    val characters = viewModel.charactersPagingFlow.collectAsLazyPagingItems()
    val filters by viewModel.filters.collectAsStateWithLifecycle()

    // Auto-select the first character when nothing is selected yet, or when the selection is no
    // longer part of the loaded list. The latter happens on filter changes: the remote mediator
    // wipes and repopulates the local cache, so a previously selected character may not exist in
    // the database anymore and its detail pane would be stuck on a loading spinner forever.
    LaunchedEffect(characters.loadState.refresh, characters.itemCount) {
        if (characters.loadState.refresh is LoadState.NotLoading && characters.itemCount > 0) {
            val selectionInList = characters.itemSnapshotList.any { it?.id == selectedId }
            if (selectedId == null || !selectionInList) {
                characters.peek(0)?.id?.let(onSelectedIdChange)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val listPaneWidth = (maxWidth * LIST_PANE_WIDTH_FRACTION)
            .coerceIn(LIST_PANE_MIN_WIDTH, LIST_PANE_MAX_WIDTH)
        val isMediumHeight = maxHeight.value >= WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
        val detailImageHeight: Dp? = if (isMediumHeight) maxHeight * DETAIL_IMAGE_HEIGHT_FRACTION else null

        Row(modifier = Modifier.fillMaxSize()) {
            CharacterListScreen(
                characters = characters,
                filters = filters,
                actions =
                    CharacterListActions(
                        onCharacterClick = onSelectedIdChange,
                        onFilterClick = onFilterClick,
                        onRemoveFilter = viewModel::removeFilter,
                        onClearFilters = viewModel::clearFilters,
                    ),
                // Scroll once to the character that was already selected when this layout appeared (carried
                // over from the single-pane detail screen, or restored after returning from filters). Later
                // taps must not scroll - the user can already see the item they tap - so the target is
                // captured at mount instead of following selectedId.
                scrollToId = remember { selectedId },
                selectedId = selectedId,
                modifier = Modifier.width(listPaneWidth),
            )
            VerticalDivider()
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (selectedId != null) {
                    val detailModifier = Modifier.widthIn(max = DETAIL_PANE_MAX_WIDTH).fillMaxHeight()
                    if (detailImageHeight != null) {
                        CharacterDetailScreen(
                            characterId = selectedId,
                            onBack = {},
                            showBackButton = false,
                            modifier = detailModifier,
                            imageHeight = detailImageHeight,
                        )
                    } else {
                        CharacterDetailScreen(
                            characterId = selectedId,
                            onBack = {},
                            showBackButton = false,
                            modifier = detailModifier,
                        )
                    }
                } else {
                    MaxSizeLoadingIndicator()
                }
            }
        }
    }
}
