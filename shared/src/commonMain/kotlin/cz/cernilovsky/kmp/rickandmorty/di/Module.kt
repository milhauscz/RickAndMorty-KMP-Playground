package cz.cernilovsky.kmp.rickandmorty.di

import cz.cernilovsky.kmp.rickandmorty.characters.di.charactersModule
import cz.cernilovsky.kmp.rickandmorty.core.db.di.databaseModule
import cz.cernilovsky.kmp.rickandmorty.core.db.di.databasePlatformModule
import cz.cernilovsky.kmp.rickandmorty.core.di.commonPlatformModule
import cz.cernilovsky.kmp.rickandmorty.core.network.di.networkModule
import cz.cernilovsky.kmp.rickandmorty.core.network.di.networkPlatformModule
import cz.cernilovsky.kmp.rickandmorty.episode.di.episodeModule
import cz.cernilovsky.kmp.rickandmorty.location.di.locationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            commonPlatformModule,
            networkModule,
            networkPlatformModule,
            databaseModule,
            databasePlatformModule,
            episodeModule,
            locationModule,
            charactersModule,
        )
    }
}
