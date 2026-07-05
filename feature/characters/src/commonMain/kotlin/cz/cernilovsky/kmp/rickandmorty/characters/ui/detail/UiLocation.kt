package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.runtime.Immutable

/**
 * UI-layer view of a location, holding only what the detail screen renders (the origin/current
 * location cards show [type] and [dimension]). Decoupled from the `:feature:location` domain model
 * so the detail UI state is Compose-stable and doesn't depend on another module's data class.
 */
@Immutable
data class UiLocation(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
)
