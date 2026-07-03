package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharacterDetailUseCase
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.ui.toMessageRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class CharacterDetailViewModel(
    private val characterId: Int,
    private val getCharacterDetail: GetCharacterDetailUseCase,
) : ViewModel() {
    private val refreshState = MutableStateFlow(RefreshState())

    val uiState: StateFlow<CharacterDetailUiState> =
        combine(getCharacterDetail.observe(characterId), refreshState) { detail, refresh ->
            CharacterDetailUiState(
                detail = detail?.toUi(),
                isLoading = refresh.isLoading,
                errorMessage = refresh.errorMessage,
            )
        }.onStart {
            // after coming back to the screen refresh will be called too, but we have offline cache so it's fine
            refresh()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CharacterDetailUiState(isLoading = true),
        )

    fun refresh() {
        viewModelScope.launch {
            refreshState.value = RefreshState(isLoading = true)
            refreshState.value =
                when (val result = getCharacterDetail.refresh(characterId)) {
                    is Result.Error -> RefreshState(errorMessage = result.error.toMessageRes())
                    is Result.Success -> RefreshState(isLoading = false)
                }
        }
    }

    private data class RefreshState(
        val isLoading: Boolean = false,
        val errorMessage: StringResource? = null,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
