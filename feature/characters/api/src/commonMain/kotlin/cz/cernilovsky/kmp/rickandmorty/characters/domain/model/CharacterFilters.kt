package cz.cernilovsky.kmp.rickandmorty.characters.domain.model

public data class CharacterFilters(
    val name: String? = null,
    val species: String? = null,
    val type: String? = null,
    val status: CharacterStatus? = null,
    val gender: CharacterGender? = null,
) {
    public val isEmpty: Boolean
        get() = this == EMPTY

    public fun without(field: CharacterFilterField): CharacterFilters =
        when (field) {
            CharacterFilterField.Name -> copy(name = null)
            CharacterFilterField.Species -> copy(species = null)
            CharacterFilterField.Type -> copy(type = null)
            CharacterFilterField.Status -> copy(status = null)
            CharacterFilterField.Gender -> copy(gender = null)
        }

    public companion object {
        public val EMPTY: CharacterFilters = CharacterFilters()
    }
}

public enum class CharacterFilterField {
    Name,
    Species,
    Type,
    Status,
    Gender,
}
