package cz.cernilovsky.kmp.rickandmorty.characters.di

import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.data.ICharactersDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharacterDetailUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailViewModel
import cz.cernilovsky.kmp.rickandmorty.characters.ui.filters.CharacterFiltersViewModel
import cz.cernilovsky.kmp.rickandmorty.characters.ui.list.CharactersViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val charactersModule =
    module {
        factoryOf(::GetCharactersUseCase)
        factoryOf(::GetCharacterDetailUseCase)
        factoryOf(::ObserveCharacterFiltersUseCase)
        factoryOf(::SetCharacterFiltersUseCase)
        singleOf(::CharactersRepository) bind ICharactersRepository::class
        singleOf(::CharactersDataSource) bind ICharactersDataSource::class
        viewModelOf(::CharactersViewModel)
        viewModelOf(::CharacterFiltersViewModel)
        viewModel { (id: Int) -> CharacterDetailViewModel(id, get()) }
    }
