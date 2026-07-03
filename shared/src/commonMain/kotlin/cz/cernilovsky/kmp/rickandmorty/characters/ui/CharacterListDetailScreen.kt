package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailScreen
import org.koin.compose.viewmodel.koinViewModel

private val LIST_PANE_WIDTH = 360.dp

/**
 * Two-pane list/detail layout used on Expanded-width windows. The character list is shown in the
 * start pane and the selected character's detail in the end pane. The first character is selected
 * automatically once the list has loaded.
 */
@Composable
fun CharacterListDetailScreen(onFilterClick: () -> Unit) {
    val viewModel = koinViewModel<CharactersViewModel>()
    val characters = viewModel.charactersPagingFlow.collectAsLazyPagingItems()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }

    // Auto-select the first character once the list has content and nothing is selected yet.
    LaunchedEffect(characters.itemCount) {
        if (selectedId == null && characters.itemCount > 0) {
            selectedId = characters.peek(0)?.id
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        CharacterListScreen(
            characters = characters,
            filters = filters,
            actions =
                CharacterListActions(
                    onCharacterClick = { selectedId = it },
                    onFilterClick = onFilterClick,
                    onRemoveFilter = viewModel::removeFilter,
                    onClearFilters = viewModel::clearFilters,
                ),
            selectedId = selectedId,
            modifier = Modifier.width(LIST_PANE_WIDTH),
        )
        VerticalDivider()
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val id = selectedId
            if (id != null) {
                CharacterDetailScreen(
                    characterId = id,
                    onBack = {},
                    showBackButton = false,
                )
            } else {
                MaxSizeLoadingIndicator()
            }
        }
    }
}
