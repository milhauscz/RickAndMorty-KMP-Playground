package cz.cernilovsky.android.rickandmorty.characters.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
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
        is LoadStateLoading -> LoadingIndicator()
        is LoadStateError -> ErrorMessage(
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
    LazyColumn {
        items(
            count = characters.itemCount,
            key = { index -> characters[index]?.id ?: index }
        ) { index ->
            val character = characters[index]
            if (character != null) {
                Text(text = character.name)
            }
        }
    }
}