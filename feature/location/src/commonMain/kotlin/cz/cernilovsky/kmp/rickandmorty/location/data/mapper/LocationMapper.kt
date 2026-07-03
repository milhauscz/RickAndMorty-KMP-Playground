package cz.cernilovsky.kmp.rickandmorty.location.data.mapper

import cz.cernilovsky.kmp.rickandmorty.location.data.local.LocationEntity
import cz.cernilovsky.kmp.rickandmorty.location.data.remote.LocationDto
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location

fun LocationDto.toEntity(): LocationEntity =
    LocationEntity(
        id = id,
        name = name,
        type = type,
        dimension = dimension,
        url = url,
        created = created,
    )

fun LocationEntity.toDomain(): Location =
    Location(
        id = id,
        name = name,
        type = type,
        dimension = dimension,
        url = url,
        created = created,
    )
