package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.runtime.Immutable
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi

/**
 * Location summary shown on the character detail screen (origin / last known).
 *
 * Decoupled from `:feature:location:api` so detail UI state stays Compose-stable without depending
 * on another module's data class.
 */
@Immutable
@InternalRickAndMortyApi
public data class UiLocation(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
)
