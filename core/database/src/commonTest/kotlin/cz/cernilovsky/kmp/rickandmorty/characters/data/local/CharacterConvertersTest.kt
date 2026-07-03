package cz.cernilovsky.kmp.rickandmorty.characters.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CharacterConvertersTest {
    private val converters = CharacterConverters()

    @Test
    fun `string list survives encode then decode`() {
        val original = listOf("https://episode/1", "https://episode/2")

        val restored = converters.toStringList(converters.fromStringList(original))

        assertEquals(original, restored)
    }

    @Test
    fun `empty string list survives round trip`() {
        val restored = converters.toStringList(converters.fromStringList(emptyList()))

        assertEquals(emptyList(), restored)
    }

    @Test
    fun `status converts to its name and back`() {
        for (status in CharacterStatusEntity.entries) {
            assertEquals(status, converters.toCharacterStatusEntity(converters.fromCharacterStatusEntity(status)))
        }
    }

    @Test
    fun `gender converts to its name and back`() {
        for (gender in CharacterGenderEntity.entries) {
            assertEquals(gender, converters.toCharacterGenderEntity(converters.fromCharacterGenderEntity(gender)))
        }
    }

    @Test
    fun `unknown status name throws`() {
        assertFailsWith<IllegalArgumentException> {
            converters.toCharacterStatusEntity("NotAStatus")
        }
    }
}
