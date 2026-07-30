package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource

@Immutable
data class CharacterDetailUiState(
    val detail: UiCharacterDetail? = null,
    val isLoading: Boolean = true,
    val errorMessage: StringResource? = null,
)
