package cz.cernilovsky.kmp.rickandmorty.episode.data.mapper

import cz.cernilovsky.kmp.rickandmorty.episode.data.remote.EpisodeDto
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodeMapperTest {
    @Test
    fun `dto to entity to domain preserves fields`() {
        val dto =
            EpisodeDto(
                id = 1,
                name = "Pilot",
                airDate = "December 2, 2013",
                episode = "S01E01",
                url = "https://rickandmortyapi.com/api/episode/1",
                created = "2017-11-10T12:56:33.798Z",
            )

        val domain = dto.toEntity().toDomain()

        assertEquals(dto.id, domain.id)
        assertEquals(dto.name, domain.name)
        assertEquals(dto.airDate, domain.airDate)
        assertEquals(dto.episode, domain.episode)
        assertEquals(dto.url, domain.url)
        assertEquals(dto.created, domain.created)
    }
}
