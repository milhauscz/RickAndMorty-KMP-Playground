package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilterField
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterLocation
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.core.ui.registerSharedElement
import cz.cernilovsky.kmp.rickandmorty.core.ui.toMessageRes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import rickandmorty.shared.generated.resources.Res
import rickandmorty.shared.generated.resources.app_title
import rickandmorty.shared.generated.resources.button_clear_all
import rickandmorty.shared.generated.resources.button_filters
import rickandmorty.shared.generated.resources.button_retry
import rickandmorty.shared.generated.resources.empty_no_characters_match
import rickandmorty.shared.generated.resources.filter_label_name
import rickandmorty.shared.generated.resources.filter_label_species
import rickandmorty.shared.generated.resources.filter_label_type
import rickandmorty.shared.generated.resources.last_known_location

data class CharacterListActions(
    val onCharacterClick: (Int) -> Unit = {},
    val onFilterClick: () -> Unit = {},
    val onRemoveFilter: (CharacterFilterField) -> Unit = {},
    val onClearFilters: () -> Unit = {},
)

@Composable
fun CharacterListScreen(
    onCharacterClick: (Int) -> Unit,
    onFilterClick: () -> Unit,
) {
    val viewModel = koinViewModel<CharactersViewModel>()
    val characters = viewModel.charactersPagingFlow.collectAsLazyPagingItems()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    CharacterListScreen(
        characters = characters,
        filters = filters,
        actions =
            CharacterListActions(
                onCharacterClick = onCharacterClick,
                onFilterClick = onFilterClick,
                onRemoveFilter = viewModel::removeFilter,
                onClearFilters = viewModel::clearFilters,
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    characters: LazyPagingItems<UiCharacter>,
    filters: CharacterFilters = CharacterFilters.EMPTY,
    actions: CharacterListActions = CharacterListActions(),
    selectedId: Int? = null,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // rememberSaveable so this survives Compose Navigation disposing and recreating this
    // composable (e.g. visiting the detail screen and coming back) - without it, LaunchedEffect
    // reruns on every fresh mount regardless of whether filters actually changed, forcibly
    // resetting the scroll position that Navigation had otherwise correctly restored.
    var lastScrolledFiltersKey by rememberSaveable { mutableStateOf(filters.toString()) }
    // However, we do not set filters as the key, but we use refresh state. When filters are updated, it takes some time
    // before data is fetched. The process is:
    // 1. loadState.refresh is NotLoading
    // 2. user changes a filter
    // 3. loadState.refresh is Loading, but if inside the Effect requires NotLoading and doesn't fire scrolll
    // 4. loadState.refresh is NotLoading again, if condition in the Effect checks filters have changed we are
    // not loading anymore => scroll to top
    LaunchedEffect(characters.loadState.refresh) {
        val key = filters.toString()
        if (key != lastScrolledFiltersKey && characters.loadState.refresh is LoadState.NotLoading) {
            lastScrolledFiltersKey = key
            listState.scrollToItem(0)
        }
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        // Top is handled by TopAppBar's own insets; bottom is handled manually below so list
        // items can scroll under the navigation bar. Horizontal insets (e.g. a landscape camera
        // cutout) are left to Scaffold's default handling so content isn't drawn behind them.
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.app_title)) },
                actions = {
                    IconButton(onClick = actions.onFilterClick) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(Res.string.button_filters),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            if (!filters.isEmpty) {
                ActiveFiltersRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    filters = filters,
                    onRemoveFilter = actions.onRemoveFilter,
                    onClearFilters = actions.onClearFilters,
                )
            }
            when (val refresh = characters.loadState.refresh) {
                is LoadState.Loading -> MaxSizeLoadingIndicator()
                is LoadState.Error ->
                    ErrorMessage(
                        error = refresh.error.toMessageRes(),
                        onRetryClicked = characters::retry,
                    )

                is LoadState.NotLoading ->
                    when {
                        // we have some items - show them
                        characters.itemCount > 0 ->
                            CharacterList(
                                characters = characters,
                                onCharacterClick = actions.onCharacterClick,
                                listState = listState,
                                selectedId = selectedId,
                            )
                        // items empty and endOfPagination == true => we didn't find anything => empty message
                        characters.loadState.source.append.endOfPaginationReached -> EmptyFilteredMessage()
                        // items empty and endOfPagination == false => we are still settling in => show progress
                        else -> MaxSizeLoadingIndicator()
                    }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveFiltersRow(
    modifier: Modifier = Modifier,
    filters: CharacterFilters,
    onRemoveFilter: (CharacterFilterField) -> Unit,
    onClearFilters: () -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.name?.let {
            FilterInputChip(stringResource(Res.string.filter_label_name, it)) {
                onRemoveFilter(CharacterFilterField.Name)
            }
        }
        filters.species?.let {
            FilterInputChip(stringResource(Res.string.filter_label_species, it)) {
                onRemoveFilter(CharacterFilterField.Species)
            }
        }
        filters.type?.let {
            FilterInputChip(stringResource(Res.string.filter_label_type, it)) {
                onRemoveFilter(CharacterFilterField.Type)
            }
        }
        filters.status?.let { status ->
            FilterInputChip(stringResource(status.toStringResource())) { onRemoveFilter(CharacterFilterField.Status) }
        }
        filters.gender?.let { gender ->
            FilterInputChip(stringResource(gender.toStringResource())) { onRemoveFilter(CharacterFilterField.Gender) }
        }
        TextButton(onClick = onClearFilters) {
            Text(text = stringResource(Res.string.button_clear_all))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterInputChip(
    label: String,
    onRemove: () -> Unit,
) {
    InputChip(
        selected = false,
        onClick = onRemove,
        label = { Text(text = label) },
        trailingIcon = { Icon(imageVector = Icons.Default.Close, contentDescription = null) },
    )
}

@Composable
fun EmptyFilteredMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.empty_no_characters_match),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun MaxSizeLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(
    error: StringResource,
    onRetryClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(error),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(
            modifier = Modifier.height(16.dp),
        )
        Button(
            onClick = onRetryClicked,
        ) {
            Text(
                text = stringResource(Res.string.button_retry),
            )
        }
    }
}

@Composable
fun CharacterList(
    characters: LazyPagingItems<UiCharacter>,
    onCharacterClick: (Int) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    selectedId: Int? = null,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding =
            WindowInsets(left = 16.dp, right = 16.dp, bottom = 16.dp)
                .add(WindowInsets.navigationBars)
                .asPaddingValues(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            count = characters.itemCount,
            key = characters.itemKey { character -> character.id },
            contentType = characters.itemContentType(),
        ) { index ->
            val character = characters[index]
            if (character != null) {
                Character(
                    character = character,
                    onClick = { onCharacterClick(character.id) },
                    isSelected = character.id == selectedId,
                )
            }
        }
        when (val appendState = characters.loadState.append) {
            is LoadState.Loading -> item { LoadingItemsIndicator() }
            is LoadState.Error ->
                item {
                    LoadingItemsError(
                        appendState.error.toMessageRes(),
                        onRetry = characters::retry,
                    )
                }
            is LoadState.NotLoading -> { /* do  nothing */ }
        }
    }
}

@Composable
fun LoadingItemsIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LoadingItemsError(
    errorMessage: StringResource,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.error,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(errorMessage),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onRetry,
            ) {
                Text(
                    text = stringResource(Res.string.button_retry),
                )
            }
        }
    }
}

@Composable
fun Character(
    character: UiCharacter,
    onClick: () -> Unit = {},
    isSelected: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth(),
        color =
            if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
        border =
            if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                null
            },
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = character.image,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .size(170.dp)
                    .registerSharedElement(createKeyForSharedTransitionAvatarUrl(character.image)),
            )
            Column(
                modifier = Modifier.weight(1f).padding(8.dp),
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(8.dp).background(color = character.status.dotColor(), shape = CircleShape),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stringResource(character.status.toStringResource())} - ${character.species}",
                        style = MaterialTheme.typography.labelMediumEmphasized,
                    )
                }
                Spacer(
                    modifier = Modifier.height(16.dp),
                )
                Text(
                    text = stringResource(Res.string.last_known_location),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = character.location.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
fun CharacterPreview() {
    Character(
        UiCharacter(
            id = 5,
            name = "Rick Sanchez",
            status = CharacterStatus.Alive,
            species = "Human",
            location =
                CharacterLocation(
                    name = "Citadel of Ricks",
                    url = "https://www.rickandmortyapi.com/location/citadel_of_ricks",
                ),
            image = "https://www.rickandmortyapi.com/character/image/5",
        ),
    )
}
