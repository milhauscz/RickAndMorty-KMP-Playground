package cz.cernilovsky.kmp.rickandmorty.characters.di

import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.data.CharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.data.ICharactersDataSource
import cz.cernilovsky.kmp.rickandmorty.characters.domain.ICharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharacterDetailUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailViewModel
import cz.cernilovsky.kmp.rickandmorty.characters.ui.filters.CharacterFiltersViewModel
import cz.cernilovsky.kmp.rickandmorty.characters.ui.list.CharactersViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
        factoryOf(::ObserveSelectedCharacterIdUseCase)
        factoryOf(::SetSelectedCharacterIdUseCase)
        // Long-lived scope for the repository's fire-and-forget selection persistence, outliving any
        // single ViewModel. SupervisorJob so one failed write can't cancel the rest.
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
        singleOf(::CharactersRepository) bind ICharactersRepository::class
        singleOf(::CharactersDataSource) bind ICharactersDataSource::class
        viewModelOf(::CharactersViewModel)
        viewModelOf(::CharacterFiltersViewModel)
        viewModel { (id: Int) -> CharacterDetailViewModel(id, get()) }
    }
