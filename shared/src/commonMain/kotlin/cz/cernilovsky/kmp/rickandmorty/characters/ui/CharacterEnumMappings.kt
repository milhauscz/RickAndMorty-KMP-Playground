package cz.cernilovsky.kmp.rickandmorty.characters.ui

import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import org.jetbrains.compose.resources.StringResource
import rickandmorty.shared.generated.resources.Res
import rickandmorty.shared.generated.resources.character_gender_female
import rickandmorty.shared.generated.resources.character_gender_genderless
import rickandmorty.shared.generated.resources.character_gender_male
import rickandmorty.shared.generated.resources.character_gender_unknown
import rickandmorty.shared.generated.resources.character_status_alive
import rickandmorty.shared.generated.resources.character_status_dead
import rickandmorty.shared.generated.resources.character_status_unknown

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
