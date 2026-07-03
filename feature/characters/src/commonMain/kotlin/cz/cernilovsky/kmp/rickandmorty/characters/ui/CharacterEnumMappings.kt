package cz.cernilovsky.kmp.rickandmorty.characters.ui

import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.Res
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.character_gender_female
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.character_gender_genderless
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.character_gender_male
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.character_gender_unknown
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.character_status_alive
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.character_status_dead
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.character_status_unknown
import org.jetbrains.compose.resources.StringResource

fun CharacterStatus.toStringResource(): StringResource =
    when (this) {
        CharacterStatus.Alive -> Res.string.character_status_alive
        CharacterStatus.Dead -> Res.string.character_status_dead
        CharacterStatus.Unknown -> Res.string.character_status_unknown
    }

fun CharacterGender.toStringResource(): StringResource =
    when (this) {
        CharacterGender.Female -> Res.string.character_gender_female
        CharacterGender.Male -> Res.string.character_gender_male
        CharacterGender.Genderless -> Res.string.character_gender_genderless
        CharacterGender.Unknown -> Res.string.character_gender_unknown
    }
