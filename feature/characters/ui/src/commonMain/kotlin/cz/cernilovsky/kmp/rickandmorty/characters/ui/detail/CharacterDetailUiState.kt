package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.runtime.Immutable
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import org.jetbrains.compose.resources.StringResource

@Immutable
@InternalRickAndMortyApi
public data class CharacterDetailUiState(
    val detail: UiCharacterDetail? = null,
    val isLoading: Boolean = true,
    val errorMessage: StringResource? = null,
)
