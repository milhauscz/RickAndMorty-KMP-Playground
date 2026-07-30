package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.ui.graphics.Color
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus

fun CharacterStatus.dotColor(): Color =
    when (this) {
        CharacterStatus.Alive -> Color.Green
        CharacterStatus.Dead -> Color.Red
        CharacterStatus.Unknown -> Color.Gray
    }

fun createKeyForSharedTransitionAvatarUrl(url: String) = "avatar_$url"
