package cz.cernilovsky.kmp.rickandmorty.episode.di

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeDataSource
import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeDataSourceKtorImpl
import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeRepositoryImpl
import cz.cernilovsky.kmp.rickandmorty.episode.domain.EpisodeRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

@InternalRickAndMortyApi
public val episodeModule: Module =
    module {
        singleOf(::EpisodeRepositoryImpl) bind EpisodeRepository::class
        singleOf(::EpisodeDataSourceKtorImpl) bind EpisodeDataSource::class
    }
