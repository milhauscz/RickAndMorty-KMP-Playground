package cz.cernilovsky.kmp.rickandmorty.characters.di

import cz.cernilovsky.kmp.rickandmorty.characters.ui.CharactersViewModel
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailViewModel
import cz.cernilovsky.kmp.rickandmorty.characters.ui.filters.CharacterFiltersViewModel
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.runtime.SdkWidgetMarker
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@InternalRickAndMortyApi
public val charactersUiModule: Module =
    module {
        single { SdkWidgetMarker() }
        viewModelOf(::CharactersViewModel)
        viewModelOf(::CharacterFiltersViewModel)
        viewModel { (id: Int) -> CharacterDetailViewModel(id, get()) }
    }
