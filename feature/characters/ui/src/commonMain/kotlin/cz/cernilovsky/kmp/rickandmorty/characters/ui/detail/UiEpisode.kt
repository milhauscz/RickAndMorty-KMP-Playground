package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.runtime.Immutable
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi

/**
 * Episode summary shown on the character detail screen.
 *
 * Decoupled from `:feature:episode:api` so detail UI state stays Compose-stable without depending
 * on another module's data class.
 */
@Immutable
@InternalRickAndMortyApi
public data class UiEpisode(
    val id: Int,
    val name: String,
    val airDate: String,
    val episode: String,
)
