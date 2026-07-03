package cz.cernilovsky.kmp.rickandmorty.episode.di

import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeDataSource
import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.episode.data.IEpisodeDataSource
import cz.cernilovsky.kmp.rickandmorty.episode.domain.IEpisodeRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val episodeModule =
    module {
        singleOf(::EpisodeRepository) bind IEpisodeRepository::class
        singleOf(::EpisodeDataSource) bind IEpisodeDataSource::class
    }
