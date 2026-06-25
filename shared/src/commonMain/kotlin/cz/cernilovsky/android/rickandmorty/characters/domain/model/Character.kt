package cz.cernilovsky.android.rickandmorty.characters.domain.model

data class Character(
    val id: Int,
    val name: String,
    val status: CharacterStatus,
    val species: String,
    val type: String,
    val gender: CharacterGender,
    val origin: CharacterLocation,
    val location: CharacterLocation,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String,
)

enum class CharacterStatus {
    Alive,
    Dead,
    Unknown,
}

enum class CharacterGender {
    Female,
    Male,
    Genderless,
    Unknown,
}
