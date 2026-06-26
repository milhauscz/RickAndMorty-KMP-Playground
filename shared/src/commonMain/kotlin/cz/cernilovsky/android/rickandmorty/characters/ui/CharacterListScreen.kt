package cz.cernilovsky.android.rickandmorty.characters.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import cz.cernilovsky.android.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharacterLocation
import cz.cernilovsky.android.rickandmorty.characters.domain.model.CharacterStatus
import org.koin.compose.viewmodel.koinViewModel

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
        is LoadState.Error -> ErrorMessage(
            refresh.error.message ?: "Unknown error"
        )

        else -> CharacterList(characters)
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(error: String) {
    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CharacterList(characters: LazyPagingItems<UiCharacter>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = characters.itemCount,
            key = characters.itemKey { character -> character.id },
            contentType = characters.itemContentType()
        ) { index ->
            val character = characters[index]
            if (character != null) {
                Character(character)
            }
        }
    }
}

@Composable
fun Character(character: UiCharacter) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(PaddingValues(horizontal = 0.dp, vertical = 8.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = character.image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(170.dp)
            )
            Column(
                modifier = Modifier.weight(1f).padding(8.dp),
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${character.status.name} - ${character.species}",
                    style = MaterialTheme.typography.labelMediumEmphasized
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                Text(
                    text = "Last known location:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = character.location.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun CharacterPreview() {
    Character(UiCharacter(
        id = 5,
        name = "Rick Sanchez",
        status = CharacterStatus.Alive,
        species = "Human",
        type = "Alive",
        gender = CharacterGender.Male,
        origin = CharacterLocation(name = "Earth", url = "https://www.rickandmortyapi.com/location/earth"),
        location = CharacterLocation(name = "Citadel of Ricks", url = "https://www.rickandmortyapi.com/location/citadel_of_ricks"),
        image = "https://www.rickandmortyapi.com/character/image/5",
        episode = emptyList(),
        url = "https://www.rickandmortyapi.com/characters/5",
        created = "March 3 1996",
        favorite = false
    ))
}