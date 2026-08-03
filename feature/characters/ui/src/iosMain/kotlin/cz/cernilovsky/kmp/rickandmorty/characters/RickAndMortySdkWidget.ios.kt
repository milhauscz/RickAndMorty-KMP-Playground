package cz.cernilovsky.kmp.rickandmorty.characters

import cz.cernilovsky.kmp.rickandmorty.characters.di.charactersUiModule
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdk
import cz.cernilovsky.kmp.rickandmorty.runtime.RickAndMortySdkConfig
import cz.cernilovsky.kmp.rickandmorty.runtime.SdkMode
import cz.cernilovsky.kmp.rickandmorty.runtime.initialize
import org.koin.core.module.Module

/**
 * Initializes the SDK for Compose character screens ([SdkMode.Widget]).
 *
 * Automatically registers [charactersUiModule]. Prefer this over plain [initialize] when hosting
 * screens from this artifact.
 */
public fun RickAndMortySdk.initializeWidget(
    config: RickAndMortySdkConfig = RickAndMortySdkConfig.builder().mode(SdkMode.Widget).build(),
    extraModules: List<Module> = emptyList(),
) {
    initialize(
        config = config.withMode(SdkMode.Widget),
        extraModules = listOf(charactersUiModule) + extraModules,
    )
}
