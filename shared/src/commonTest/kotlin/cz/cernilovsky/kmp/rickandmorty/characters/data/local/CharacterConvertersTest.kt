package cz.cernilovsky.kmp.rickandmorty.characters.data.local

import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
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
        for (status in CharacterStatus.entries) {
            assertEquals(status, converters.toCharacterStatus(converters.fromCharacterStatus(status)))
        }
    }

    @Test
    fun `gender converts to its name and back`() {
        for (gender in CharacterGender.entries) {
            assertEquals(gender, converters.toCharacterGender(converters.fromCharacterGender(gender)))
        }
    }

    @Test
    fun `unknown status name throws`() {
        assertFailsWith<IllegalArgumentException> {
            converters.toCharacterStatus("NotAStatus")
        }
    }
}
