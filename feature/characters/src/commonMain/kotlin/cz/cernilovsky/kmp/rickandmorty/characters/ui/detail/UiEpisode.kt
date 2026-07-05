package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.runtime.Immutable

/**
 * UI-layer view of an episode, holding only what the detail screen renders (the episode carousel
 * shows [episode], [name] and [airDate]). Decoupled from the `:feature:episode` domain model so the
 * detail UI state is Compose-stable and doesn't depend on another module's data class.
 */
@Immutable
data class UiEpisode(
    val id: Int,
    val name: String,
    val airDate: String,
    val episode: String,
)
