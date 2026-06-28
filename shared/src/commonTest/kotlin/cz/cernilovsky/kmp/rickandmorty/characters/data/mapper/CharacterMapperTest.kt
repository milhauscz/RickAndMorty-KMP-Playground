package cz.cernilovsky.kmp.rickandmorty.characters.data.mapper

import cz.cernilovsky.kmp.rickandmorty.characters.characterDto
import cz.cernilovsky.kmp.rickandmorty.characters.data.remote.CharactersResponseDto
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.core.data.model.InfoDto
import kotlin.test.Test
import kotlin.test.assertEquals

class CharacterMapperTest {

    @Test
    fun `maps all scalar and nested fields from dto to domain`() {
        val dto = characterDto(id = 42, name = "Morty Smith")

        val domain = dto.toDomain()

        assertEquals(42, domain.id)
        assertEquals("Morty Smith", domain.name)
        assertEquals("Human", domain.species)
        assertEquals("Earth (C-137)", domain.origin.name)
        assertEquals("https://origin", domain.origin.url)
        assertEquals("Citadel of Ricks", domain.location.name)
        assertEquals(listOf("https://episode/1", "https://episode/2"), domain.episode)
    }

    @Test
    fun `maps known status strings to enum`() {
        assertEquals(CharacterStatus.Alive, characterDto(status = "Alive").toDomain().status)
        assertEquals(CharacterStatus.Dead, characterDto(status = "Dead").toDomain().status)
    }

    @Test
    fun `maps unrecognized status to Unknown`() {
        assertEquals(CharacterStatus.Unknown, characterDto(status = "unknown").toDomain().status)
        assertEquals(CharacterStatus.Unknown, characterDto(status = "anything else").toDomain().status)
    }

    @Test
    fun `maps known gender strings to enum`() {
        assertEquals(CharacterGender.Female, characterDto(gender = "Female").toDomain().gender)
        assertEquals(CharacterGender.Male, characterDto(gender = "Male").toDomain().gender)
        assertEquals(CharacterGender.Genderless, characterDto(gender = "Genderless").toDomain().gender)
    }

    @Test
    fun `maps unrecognized gender to Unknown`() {
        assertEquals(CharacterGender.Unknown, characterDto(gender = "unknown").toDomain().gender)
    }

    @Test
    fun `maps response info and result list`() {
        val response = CharactersResponseDto(
            info = InfoDto(count = 2, pages = 1, next = null, prev = null),
            results = listOf(characterDto(id = 1), characterDto(id = 2)),
        )

        val domain = response.toDomain()

        assertEquals(2, domain.info.count)
        assertEquals(1, domain.info.pages)
        assertEquals(listOf(1, 2), domain.characters.map { it.id })
    }
}
