package cz.cernilovsky.kmp.rickandmorty.location.di

import cz.cernilovsky.kmp.rickandmorty.location.data.ILocationDataSource
import cz.cernilovsky.kmp.rickandmorty.location.data.LocationDataSource
import cz.cernilovsky.kmp.rickandmorty.location.data.LocationRepository
import cz.cernilovsky.kmp.rickandmorty.location.domain.ILocationRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val locationModule =
    module {
        singleOf(::LocationRepository) bind ILocationRepository::class
        singleOf(::LocationDataSource) bind ILocationDataSource::class
    }
