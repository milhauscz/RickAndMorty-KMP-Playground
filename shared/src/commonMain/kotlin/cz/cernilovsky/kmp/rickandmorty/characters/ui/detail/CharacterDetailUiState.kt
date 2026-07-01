package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import org.jetbrains.compose.resources.StringResource

data class CharacterDetailUiState(
    val detail: UiCharacterDetail? = null,
    val isLoading: Boolean = true,
    val errorMessage: StringResource? = null,
)
