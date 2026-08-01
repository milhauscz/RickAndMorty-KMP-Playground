package cz.cernilovsky.kmp.rickandmorty.episode.data.mapper

import cz.cernilovsky.kmp.rickandmorty.episode.data.local.EpisodeEntity
import cz.cernilovsky.kmp.rickandmorty.episode.data.remote.EpisodeDto
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode

internal fun EpisodeDto.toEntity(): EpisodeEntity =
    EpisodeEntity(
        id = id,
        name = name,
        airDate = airDate,
        episode = episode,
        url = url,
        created = created,
    )

internal fun EpisodeEntity.toDomain(): Episode =
    Episode(
        id = id,
        name = name,
        airDate = airDate,
        episode = episode,
        url = url,
        created = created,
    )
