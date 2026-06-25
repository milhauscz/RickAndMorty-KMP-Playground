package cz.cernilovsky.android.rickandmorty.di

import cz.cernilovsky.android.rickandmorty.characters.data.CharactersDataSource
import cz.cernilovsky.android.rickandmorty.characters.data.CharactersRepository
import cz.cernilovsky.android.rickandmorty.characters.data.ICharactersDataSource
import cz.cernilovsky.android.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.android.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.android.rickandmorty.characters.ui.CharactersViewModel
import cz.cernilovsky.android.rickandmorty.core.network.HttpClientFactory
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
}