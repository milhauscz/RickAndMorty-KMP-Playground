package cz.cernilovsky.kmp.rickandmorty.characters.data

import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharactersUrlBuilderTest {
    @Test
    fun `no filters returns base url without query`() {
        val url = buildCharactersUrl(CharacterFilters.EMPTY)

        assertEquals("${NetworkConfig.BASE_URL}/character", url)
    }

    @Test
    fun `all filters produce lowercase enum params in order`() {
        val url =
            buildCharactersUrl(
                CharacterFilters(
                    name = "rick",
                    species = "Human",
                    type = "Scientist",
                    status = CharacterStatus.Alive,
                    gender = CharacterGender.Male,
                ),
            )

        assertEquals(
            "${NetworkConfig.BASE_URL}/character?name=rick&species=Human&type=Scientist&status=alive&gender=male",
            url,
        )
    }

    @Test
    fun `values with spaces are percent encoded`() {
        val url = buildCharactersUrl(CharacterFilters(name = "rick sanchez"))

        assertTrue(url.contains("name=rick+sanchez") || url.contains("name=rick%20sanchez"))
    }
}
