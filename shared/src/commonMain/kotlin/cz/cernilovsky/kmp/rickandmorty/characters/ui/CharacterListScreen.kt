package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterLocation
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.core.ui.toMessageRes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import rickandmorty.shared.generated.resources.Res
import rickandmorty.shared.generated.resources.button_retry
import rickandmorty.shared.generated.resources.last_known_location

@Composable
fun CharacterListScreen() {
    val viewModel = koinViewModel<CharactersViewModel>()
    val characters = viewModel.charactersPagingFlow.collectAsLazyPagingItems()
    CharacterListScreen(characters)
}

@Composable
fun CharacterListScreen(characters: LazyPagingItems<UiCharacter>) {
    when (val refresh = characters.loadState.refresh) {
        is LoadState.Loading -> LoadingIndicator()
        is LoadState.Error ->
            ErrorMessage(
                error = refresh.error.toMessageRes(),
                onRetryClicked = characters::retry,
            )

        else -> CharacterList(characters)
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxHeight(),
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
fun CharacterList(characters: LazyPagingItems<UiCharacter>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            count = characters.itemCount,
            key = characters.itemKey { character -> character.id },
            contentType = characters.itemContentType(),
        ) { index ->
            val character = characters[index]
            if (character != null) {
                Character(character)
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
fun Character(character: UiCharacter) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(PaddingValues(horizontal = 0.dp, vertical = 8.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = character.image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(170.dp),
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
                    val dotColor =
                        when (character.status) {
                            CharacterStatus.Alive -> Color.Green
                            CharacterStatus.Dead -> Color.Red
                            CharacterStatus.Unknown -> Color.Gray
                        }
                    Box(
                        modifier = Modifier.size(8.dp).background(color = dotColor, shape = CircleShape),
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
                Spacer(
                    modifier = Modifier.height(16.dp),
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
