package cz.cernilovsky.kmp.rickandmorty.characters.data.mapper

import cz.cernilovsky.kmp.rickandmorty.characters.characterDto
import kotlin.test.Test
import kotlin.test.assertEquals

class CharacterEntityMapperTest {
    @Test
    fun `domain to entity and back preserves all fields`() {
        val original = characterDto(id = 7, name = "Birdperson").toDomain()

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `dto to entity maps nested location`() {
        val entity = characterDto(id = 7).toEntity()

        assertEquals(7, entity.id)
        assertEquals("Earth (C-137)", entity.origin.name)
        assertEquals("Citadel of Ricks", entity.location.name)
    }
}
