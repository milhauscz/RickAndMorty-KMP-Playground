package cz.cernilovsky.kmp.rickandmorty.characters.di

import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersDataSourceKtorImpl
import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersRepositoryImpl
import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharacterDetailUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

@InternalRickAndMortyApi
public val charactersModule: Module =
    module {
        factoryOf(::GetCharactersUseCase)
        factoryOf(::GetCharacterDetailUseCase)
        factoryOf(::ObserveCharacterFiltersUseCase)
        factoryOf(::SetCharacterFiltersUseCase)
        factoryOf(::ObserveSelectedCharacterIdUseCase)
        factoryOf(::SetSelectedCharacterIdUseCase)
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
        singleOf(::CharactersRepositoryImpl) bind CharactersRepository::class
        singleOf(::CharactersDataSourceKtorImpl) bind CharactersDataSource::class
    }
