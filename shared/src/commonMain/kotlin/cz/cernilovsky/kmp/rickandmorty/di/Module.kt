package cz.cernilovsky.kmp.rickandmorty.di

import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.data.ICharactersDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharacterDetailUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.ui.CharactersViewModel
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailViewModel
import cz.cernilovsky.kmp.rickandmorty.characters.ui.filters.CharacterFiltersViewModel
import cz.cernilovsky.kmp.rickandmorty.core.db.AppDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.DatabaseConfig
import cz.cernilovsky.kmp.rickandmorty.core.db.getAppDatabase
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientFactory
import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeDataSource
import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.episode.data.EpisodeRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.episode.data.IEpisodeDataSource
import cz.cernilovsky.kmp.rickandmorty.episode.domain.IEpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.location.data.ILocationDataSource
import cz.cernilovsky.kmp.rickandmorty.location.data.LocationDataSource
import cz.cernilovsky.kmp.rickandmorty.location.data.LocationRepository
import cz.cernilovsky.kmp.rickandmorty.location.data.LocationRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.location.domain.ILocationRepository
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(platformModule, sharedModule)
    }
}

expect val platformModule: Module

val sharedModule =
    module {
        single<HttpClient> {
            HttpClientFactory.create(get())
        }
        factoryOf(::GetCharactersUseCase)
        factoryOf(::GetCharacterDetailUseCase)
        factoryOf(::ObserveCharacterFiltersUseCase)
        factoryOf(::SetCharacterFiltersUseCase)
        singleOf(::CharactersRepository) bind ICharactersRepository::class
        singleOf(::CharactersDataSource) bind ICharactersDataSource::class
        singleOf(::LocationRepository) bind ILocationRepository::class
        singleOf(::LocationDataSource) bind ILocationDataSource::class
        singleOf(::EpisodeRepository) bind IEpisodeRepository::class
        singleOf(::EpisodeDataSource) bind IEpisodeDataSource::class
        viewModelOf(::CharactersViewModel)
        viewModelOf(::CharacterFiltersViewModel)
        viewModel { (id: Int) -> CharacterDetailViewModel(id, get()) }
        single<AppDatabase> {
            getAppDatabase(get(), get<DatabaseConfig>().allowDestructiveMigration)
        }
        single<CharactersRoomDataSource> {
            get<AppDatabase>().charactersDao()
        }
        single<LocationRoomDataSource> {
            get<AppDatabase>().locationDao()
        }
        single<EpisodeRoomDataSource> {
            get<AppDatabase>().episodeDao()
        }
    }
