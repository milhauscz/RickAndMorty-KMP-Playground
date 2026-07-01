package cz.cernilovsky.kmp.rickandmorty.location.data.mapper

import cz.cernilovsky.kmp.rickandmorty.location.data.remote.LocationDto
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationMapperTest {
    @Test
    fun `dto to entity to domain preserves fields`() {
        val dto =
            LocationDto(
                id = 1,
                name = "Earth (C-137)",
                type = "Planet",
                dimension = "Dimension C-137",
                url = "https://rickandmortyapi.com/api/location/1",
                created = "2017-11-10T12:42:04.162Z",
            )

        val domain = dto.toEntity().toDomain()

        assertEquals(dto.id, domain.id)
        assertEquals(dto.name, domain.name)
        assertEquals(dto.type, domain.type)
        assertEquals(dto.dimension, domain.dimension)
        assertEquals(dto.url, domain.url)
        assertEquals(dto.created, domain.created)
    }
}
