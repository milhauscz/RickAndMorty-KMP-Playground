package cz.cernilovsky.kmp.rickandmorty.di

import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersRoomDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.data.ICharactersDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.ui.CharactersViewModel
import cz.cernilovsky.kmp.rickandmorty.core.db.AppDatabase
import cz.cernilovsky.kmp.rickandmorty.core.db.getAppDatabase
import cz.cernilovsky.kmp.rickandmorty.core.network.HttpClientFactory
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
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

val sharedModule = module {
    single<HttpClient> {
        HttpClientFactory.create(get())
    }
    factoryOf(::GetCharactersUseCase)
    singleOf(::CharactersRepository) bind ICharactersRepository::class
    singleOf(::CharactersDataSource) bind ICharactersDataSource::class
    viewModelOf(::CharactersViewModel)
    single<AppDatabase> {
        getAppDatabase(get())
    }
    single<CharactersRoomDataSource> {
        get<AppDatabase>().charactersDao()
    }
}