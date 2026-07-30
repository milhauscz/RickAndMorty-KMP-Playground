package cz.cernilovsky.kmp.rickandmorty.characters.domain.model

data class CharacterFilters(
    val name: String? = null,
    val species: String? = null,
    val type: String? = null,
    val status: CharacterStatus? = null,
    val gender: CharacterGender? = null,
) {
    val isEmpty: Boolean
        get() = this == EMPTY

    fun without(field: CharacterFilterField): CharacterFilters =
        when (field) {
            CharacterFilterField.Name -> copy(name = null)
            CharacterFilterField.Species -> copy(species = null)
            CharacterFilterField.Type -> copy(type = null)
            CharacterFilterField.Status -> copy(status = null)
            CharacterFilterField.Gender -> copy(gender = null)
        }

    companion object {
        val EMPTY = CharacterFilters()
    }
}

enum class CharacterFilterField {
    Name,
    Species,
    Type,
    Status,
    Gender,
}
